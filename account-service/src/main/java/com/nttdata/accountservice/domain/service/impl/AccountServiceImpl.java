package com.nttdata.accountservice.domain.service.impl;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountEventAvroDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.api.dto.AccountResponseDto;
import com.nttdata.accountservice.api.dto.AccountUpdateRequestDto;
import com.nttdata.accountservice.domain.mapper.AccountMapper;
import com.nttdata.accountservice.domain.policy.AccountValidationPolicy;
import com.nttdata.accountservice.domain.port.AccountCachePort;
import com.nttdata.accountservice.domain.port.AccountEventPublisherPort;
import com.nttdata.accountservice.domain.port.AccountRepositoryPort;
import com.nttdata.accountservice.domain.port.AccountRecord;
import com.nttdata.accountservice.domain.port.CustomerValidationPort;
import com.nttdata.accountservice.domain.service.AccountService;
import com.nttdata.accountservice.generated.api.AccountsApiDelegate;
import com.nttdata.accountservice.generated.model.AccountResponse;
import com.nttdata.accountservice.generated.model.BalanceOperationRequest;
import com.nttdata.accountservice.generated.model.CreateAccountRequest;
import com.nttdata.accountservice.generated.model.UpdateAccountRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService, AccountsApiDelegate {

    private final AccountValidationPolicy accountValidationPolicy;
    private final AccountRepositoryPort accountRepositoryPort;
    private final CustomerValidationPort customerValidationPort;
    private final AccountEventPublisherPort accountEventPublisherPort;
    private final AccountCachePort accountCachePort;

    @Override
    public Mono<AccountResponseDto> create(AccountCreateRequestDto request, String requestedBy) {
        accountValidationPolicy.validateRequestedBy(requestedBy);
        accountValidationPolicy.validateCreate(request);

        return customerValidationPort.existsCustomer(request.getCustomerId(), requestedBy)
                .publishOn(Schedulers.parallel())
                .doOnNext(exists -> accountValidationPolicy.validateCustomerExists(exists, request.getCustomerId()))
                .flatMap(valid -> accountRepositoryPort.findByAccountNumber(request.getAccountNumber()))
                .flatMap(existing -> Mono.<AccountResponseDto>error(
                        new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists")))
                .switchIfEmpty(Mono.defer(() -> accountRepositoryPort
                        .save(AccountMapper.toNewRecord(request, requestedBy))
                        .flatMap(saved -> accountCachePort.putBalance(saved.accountId(), saved.balance())
                                .then(accountEventPublisherPort.publish(AccountEventAvroDto.builder()
                                        .eventType("ACCOUNT_CREATED")
                                        .accountId(saved.accountId())
                                        .customerId(saved.customerId())
                                        .balance(saved.balance())
                                        .status(saved.status())
                                        .build()))
                                .thenReturn(saved))
                        .map(AccountMapper::toDto)))
                .cast(AccountResponseDto.class)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(ex -> log.error("Error creating account accountNumber={}", request.getAccountNumber(), ex))
                .onErrorResume(DuplicateKeyException.class,
                        ex -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists")));
    }

    @Override
    public Mono<AccountResponseDto> findById(String accountId) {
        accountValidationPolicy.validateAccountId(accountId);

        return accountRepositoryPort.findById(accountId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
                .publishOn(Schedulers.parallel())
                .map(AccountMapper::toDto)
                .doOnError(ex -> log.error("Error retrieving accountId={}", accountId, ex));
    }

    @Override
    public Flux<AccountResponseDto> findAll(String customerId) {
        Flux<AccountRecord> source = customerId == null || customerId.isBlank()
                ? accountRepositoryPort.findAll()
                : accountRepositoryPort.findByCustomerId(customerId);

        return source
                .publishOn(Schedulers.parallel())
                .map(AccountMapper::toDto)
                .doOnError(ex -> log.error("Error listing accounts customerId={}", customerId, ex))
                .collectList()
                .onErrorReturn(List.of())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<AccountResponseDto> update(String accountId, AccountUpdateRequestDto request, String requestedBy) {
        accountValidationPolicy.validateRequestedBy(requestedBy);
        accountValidationPolicy.validateUpdate(accountId, request);

        return accountRepositoryPort.findById(accountId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
                .map(current -> AccountMapper.mergeStatus(current, request))
                .publishOn(Schedulers.boundedElastic())
                .flatMap(accountRepositoryPort::save)
                .flatMap(saved -> accountEventPublisherPort.publish(AccountEventAvroDto.builder()
                        .eventType("ACCOUNT_UPDATED")
                        .accountId(saved.accountId())
                        .customerId(saved.customerId())
                        .balance(saved.balance())
                        .status(saved.status())
                        .build()).thenReturn(saved))
                .map(AccountMapper::toDto)
                .doOnError(ex -> log.error("Error updating accountId={}", accountId, ex))
                .onErrorResume(DuplicateKeyException.class,
                        ex -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Duplicated account data")));
    }

    @Override
    public Mono<Void> delete(String accountId, String requestedBy) {
        accountValidationPolicy.validateRequestedBy(requestedBy);
        accountValidationPolicy.validateAccountId(accountId);

        return accountRepositoryPort.findById(accountId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
                .flatMap(existing -> accountRepositoryPort.deleteById(existing.accountId())
                        .then(accountCachePort.evictBalance(existing.accountId())))
                .doOnError(ex -> log.error("Error deleting accountId={}", accountId, ex))
                .onErrorResume(ResponseStatusException.class, Mono::error);
    }

    @Override
    public Mono<AccountResponseDto> operate(String accountId, AccountOperationRequestDto request, String requestedBy) {
        accountValidationPolicy.validateRequestedBy(requestedBy);
        accountValidationPolicy.validateOperation(accountId, request);

        return accountCachePort.registerOperation(request.getOperationId())
                .flatMap(registered -> {
                    if (!registered) {
                        return findById(accountId);
                    }
                    return accountRepositoryPort.findById(accountId)
                            .switchIfEmpty(Mono.error(
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")))
                            .map(current -> AccountMapper.applyOperation(current, request))
                            .doOnNext(next -> accountValidationPolicy.validateSufficientBalance(next.balance()))
                            .publishOn(Schedulers.boundedElastic())
                            .flatMap(accountRepositoryPort::save)
                            .flatMap(saved -> accountCachePort.putBalance(saved.accountId(), saved.balance())
                                    .then(accountEventPublisherPort.publish(AccountEventAvroDto.builder()
                                            .eventType("ACCOUNT_OPERATION")
                                            .accountId(saved.accountId())
                                            .customerId(saved.customerId())
                                            .operationId(request.getOperationId())
                                            .balance(saved.balance())
                                            .build()))
                                    .thenReturn(saved))
                            .map(AccountMapper::toDto)
                            .doOnError(ex -> log.error("Error applying operation accountId={}", accountId, ex))
                            .onErrorResume(ex -> Mono.just(new AccountResponseDto()));
                });
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(
            Mono<CreateAccountRequest> createAccountRequest,
            ServerWebExchange exchange) {
        return createAccountRequest
                .map(AccountMapper::toCreateDto)
                .flatMap(request -> create(request, authenticatedUser(exchange)))
                .map(AccountMapper::toGenerated)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccount(String accountId, ServerWebExchange exchange) {
        return delete(accountId, authenticatedUser(exchange))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(String accountId, ServerWebExchange exchange) {
        return findById(accountId)
                .map(AccountMapper::toGenerated)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAccounts(String customerId, ServerWebExchange exchange) {
        Flux<AccountResponse> body = findAll(customerId).map(AccountMapper::toGenerated);
        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> updateAccount(
            String accountId,
            Mono<UpdateAccountRequest> updateAccountRequest,
            ServerWebExchange exchange) {
        return updateAccountRequest
                .map(AccountMapper::toUpdateDto)
                .flatMap(request -> update(accountId, request, authenticatedUser(exchange)))
                .map(AccountMapper::toGenerated)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> operateBalance(
            String accountId,
            Mono<BalanceOperationRequest> balanceOperationRequest,
            ServerWebExchange exchange) {
        return balanceOperationRequest
                .map(AccountMapper::toOperationDto)
                .flatMap(request -> operate(accountId, request, authenticatedUser(exchange)))
                .map(AccountMapper::toGenerated)
                .map(ResponseEntity::ok);
    }

    private String authenticatedUser(ServerWebExchange exchange) {
        String requestedBy = exchange.getRequest().getHeaders().getFirst("X-Auth-User");
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
        }
        return requestedBy;
    }
}
