package com.nttdata.accountservice.infrastructure.mongodb;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "account.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MongoAccountDataInitializer implements CommandLineRunner {

    private final AccountMongoRepository accountMongoRepository;

    @Override
    public void run(String... args) {
        accountMongoRepository.findByAccountNumber("ACC-DEMO-001")
                .switchIfEmpty(Mono.defer(() -> accountMongoRepository.save(AccountDocument.builder()
                        .accountId("ACC-DEMO-001")
                        .customerId("CUS-DEMO")
                        .accountNumber("ACC-DEMO-001")
                        .accountType("SAVINGS")
                        .currency("PEN")
                        .status("ACTIVE")
                        .balance(BigDecimal.valueOf(500))
                        .createdBy("system")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())))
                .doOnNext(saved -> log.info("Account seed loaded accountId={}", saved.getAccountId()))
                .subscribe();
    }
}
