package com.nttdata.accountservice.infrastructure.mongodb;

import com.nttdata.accountservice.domain.port.AccountRecord;
import com.nttdata.accountservice.domain.port.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MongoAccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountMongoRepository accountMongoRepository;

    @Override
    public Mono<AccountRecord> save(AccountRecord accountRecord) {
        return accountMongoRepository.save(toDocument(accountRecord)).map(this::toRecord);
    }

    @Override
    public Mono<AccountRecord> findById(String accountId) {
        return accountMongoRepository.findById(accountId).map(this::toRecord);
    }

    @Override
    public Flux<AccountRecord> findAll() {
        return accountMongoRepository.findAll().map(this::toRecord);
    }

    @Override
    public Flux<AccountRecord> findByCustomerId(String customerId) {
        return accountMongoRepository.findByCustomerId(customerId).map(this::toRecord);
    }

    @Override
    public Mono<AccountRecord> findByAccountNumber(String accountNumber) {
        return accountMongoRepository.findByAccountNumber(accountNumber).map(this::toRecord);
    }

    @Override
    public Mono<Void> deleteById(String accountId) {
        return accountMongoRepository.deleteById(accountId);
    }

    private AccountDocument toDocument(AccountRecord record) {
        return AccountDocument.builder()
                .accountId(record.accountId())
                .customerId(record.customerId())
                .accountNumber(record.accountNumber())
                .accountType(record.accountType())
                .currency(record.currency())
                .status(record.status())
                .balance(record.balance())
                .createdBy(record.createdBy())
                .createdAt(record.createdAt())
                .updatedAt(record.updatedAt())
                .build();
    }

    private AccountRecord toRecord(AccountDocument document) {
        return new AccountRecord(
                document.getAccountId(),
                document.getCustomerId(),
                document.getAccountNumber(),
                document.getAccountType(),
                document.getCurrency(),
                document.getStatus(),
                document.getBalance(),
                document.getCreatedBy(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }
}
