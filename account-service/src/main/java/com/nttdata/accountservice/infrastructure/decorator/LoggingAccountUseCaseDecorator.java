package com.nttdata.accountservice.infrastructure.decorator;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.api.dto.AccountResponseDto;
import com.nttdata.accountservice.api.dto.AccountUpdateRequestDto;
import com.nttdata.accountservice.domain.port.AccountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LoggingAccountUseCaseDecorator implements AccountUseCase {

    private final AccountUseCase delegate;

    @Override
    public Mono<AccountResponseDto> create(AccountCreateRequestDto request, String requestedBy) {
        log.info("[AccountUseCase] create accountNumber={} by={}", request.getAccountNumber(), requestedBy);
        return delegate.create(request, requestedBy);
    }

    @Override
    public Mono<AccountResponseDto> findById(String accountId) {
        log.info("[AccountUseCase] findById accountId={}", accountId);
        return delegate.findById(accountId);
    }

    @Override
    public Flux<AccountResponseDto> findAll(String customerId) {
        log.info("[AccountUseCase] findAll customerId={}", customerId);
        return delegate.findAll(customerId);
    }

    @Override
    public Mono<AccountResponseDto> update(String accountId, AccountUpdateRequestDto request, String requestedBy) {
        log.info("[AccountUseCase] update accountId={} by={}", accountId, requestedBy);
        return delegate.update(accountId, request, requestedBy);
    }

    @Override
    public Mono<Void> delete(String accountId, String requestedBy) {
        log.info("[AccountUseCase] delete accountId={} by={}", accountId, requestedBy);
        return delegate.delete(accountId, requestedBy);
    }

    @Override
    public Mono<AccountResponseDto> operate(String accountId, AccountOperationRequestDto request, String requestedBy) {
        log.info("[AccountUseCase] operate accountId={} opType={} opId={} by={}",
                accountId, request.getOperationType(), request.getOperationId(), requestedBy);
        return delegate.operate(accountId, request, requestedBy);
    }
}
