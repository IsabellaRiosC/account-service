package com.nttdata.accountservice.domain.port;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepositoryPort {

    Mono<AccountRecord> save(AccountRecord accountRecord);

    Mono<AccountRecord> findById(String accountId);

    Flux<AccountRecord> findAll();

    Flux<AccountRecord> findByCustomerId(String customerId);

    Mono<AccountRecord> findByAccountNumber(String accountNumber);

    Mono<Void> deleteById(String accountId);
}
