package com.nttdata.accountservice.domain.port;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.api.dto.AccountResponseDto;
import com.nttdata.accountservice.api.dto.AccountUpdateRequestDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountUseCase {

    Mono<AccountResponseDto> create(AccountCreateRequestDto request, String requestedBy);

    Mono<AccountResponseDto> findById(String accountId);

    Flux<AccountResponseDto> findAll(String customerId);

    Mono<AccountResponseDto> update(String accountId, AccountUpdateRequestDto request, String requestedBy);

    Mono<Void> delete(String accountId, String requestedBy);

    Mono<AccountResponseDto> operate(String accountId, AccountOperationRequestDto request, String requestedBy);
}
