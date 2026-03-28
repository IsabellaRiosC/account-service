package com.nttdata.accountservice.infrastructure.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountMongoRepository extends ReactiveMongoRepository<AccountDocument, String> {

    Flux<AccountDocument> findByCustomerId(String customerId);

    Mono<AccountDocument> findByAccountNumber(String accountNumber);
}
