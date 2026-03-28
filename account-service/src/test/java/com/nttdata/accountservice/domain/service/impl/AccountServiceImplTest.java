package com.nttdata.accountservice.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.domain.policy.AccountValidationPolicy;
import com.nttdata.accountservice.domain.port.AccountCachePort;
import com.nttdata.accountservice.domain.port.AccountEventPublisherPort;
import com.nttdata.accountservice.domain.port.AccountRecord;
import com.nttdata.accountservice.domain.port.AccountRepositoryPort;
import com.nttdata.accountservice.domain.port.CustomerValidationPort;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private CustomerValidationPort customerValidationPort;

    @Mock
    private AccountEventPublisherPort accountEventPublisherPort;

    @Mock
    private AccountCachePort accountCachePort;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                new AccountValidationPolicy(),
                accountRepositoryPort,
                customerValidationPort,
                accountEventPublisherPort,
                accountCachePort);
    }

    @Test
    void createShouldPersistAccount() {
        AccountCreateRequestDto request = new AccountCreateRequestDto();
        request.setCustomerId("CUS-1");
        request.setAccountNumber("001-123");
        request.setAccountType("SAVINGS");
        request.setCurrency("PEN");
        request.setInitialBalance(BigDecimal.valueOf(200));

        AccountRecord saved = new AccountRecord(
                "ACC-1",
                "CUS-1",
                "001-123",
                "SAVINGS",
                "PEN",
                "ACTIVE",
                BigDecimal.valueOf(200),
                "demo.user",
                Instant.now(),
                Instant.now());

        when(customerValidationPort.existsCustomer("CUS-1", "demo.user")).thenReturn(Mono.just(true));
        when(accountRepositoryPort.findByAccountNumber("001-123")).thenReturn(Mono.empty());
        when(accountRepositoryPort.save(any(AccountRecord.class))).thenReturn(Mono.just(saved));
        when(accountCachePort.putBalance("ACC-1", BigDecimal.valueOf(200))).thenReturn(Mono.empty());
        when(accountEventPublisherPort.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.create(request, "demo.user"))
                .assertNext(response -> {
                    assertThat(response.getAccountId()).isEqualTo("ACC-1");
                    assertThat(response.getCustomerId()).isEqualTo("CUS-1");
                    assertThat(response.getBalance()).isEqualByComparingTo("200");
                })
                .verifyComplete();
    }

    @Test
    void findAllShouldReturnEmptyWhenRepositoryFails() {
        when(accountRepositoryPort.findAll()).thenReturn(Flux.error(new IllegalStateException("mongo down")));

        StepVerifier.create(accountService.findAll(null))
                .verifyComplete();
    }

    @Test
    void operateShouldUpdateBalanceOnCredit() {
        AccountRecord existing = new AccountRecord(
                "ACC-1",
                "CUS-1",
                "001-123",
                "SAVINGS",
                "PEN",
                "ACTIVE",
                BigDecimal.valueOf(100),
                "demo.user",
                Instant.now(),
                Instant.now());

        AccountOperationRequestDto request = new AccountOperationRequestDto();
        request.setAmount(BigDecimal.valueOf(50));
        request.setOperationType("CREDIT");
        request.setOperationId("OP-1");

        when(accountCachePort.registerOperation("OP-1")).thenReturn(Mono.just(true));
        when(accountRepositoryPort.findById("ACC-1")).thenReturn(Mono.just(existing));
        when(accountRepositoryPort.save(any(AccountRecord.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(accountCachePort.putBalance("ACC-1", BigDecimal.valueOf(150))).thenReturn(Mono.empty());
        when(accountEventPublisherPort.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(accountService.operate("ACC-1", request, "demo.user"))
                .assertNext(response -> assertThat(response.getBalance()).isEqualByComparingTo("150"))
                .verifyComplete();
    }
}
