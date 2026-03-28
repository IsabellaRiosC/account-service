package com.nttdata.accountservice.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.accountservice.domain.port.AccountCachePort;
import com.nttdata.accountservice.domain.port.AccountEventPublisherPort;
import com.nttdata.accountservice.domain.port.AccountRecord;
import com.nttdata.accountservice.domain.port.AccountRepositoryPort;
import com.nttdata.accountservice.domain.port.CustomerValidationPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AccountApiIntegrationTest {

    private static final String JWT_SECRET = "change_this_secret_in_real_env_min_32_chars";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AccountRepositoryPort accountRepositoryPort;

    @MockBean
    private CustomerValidationPort customerValidationPort;

    @MockBean
    private AccountEventPublisherPort accountEventPublisherPort;

    @MockBean
    private AccountCachePort accountCachePort;

    @Test
    void createAccountShouldValidateTokenAndReturnCreated() {
        AccountRecord saved = new AccountRecord(
                "ACC-1",
                "CUS-1",
                "001-123",
                "SAVINGS",
                "PEN",
                "ACTIVE",
                BigDecimal.valueOf(250),
                "demo.user",
                Instant.now(),
                Instant.now());

        when(customerValidationPort.existsCustomer("CUS-1", "demo.user")).thenReturn(Mono.just(true));
        when(accountRepositoryPort.findByAccountNumber("001-123")).thenReturn(Mono.empty());
        when(accountRepositoryPort.save(any(AccountRecord.class))).thenReturn(Mono.just(saved));
        when(accountCachePort.putBalance("ACC-1", BigDecimal.valueOf(250))).thenReturn(Mono.empty());
        when(accountEventPublisherPort.publish(any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor("demo.user"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "customerId": "CUS-1",
                          "accountNumber": "001-123",
                          "accountType": "SAVINGS",
                          "currency": "PEN",
                          "initialBalance": 250
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accountId").isEqualTo("ACC-1")
                .jsonPath("$.createdBy").isEqualTo("demo.user");
    }

    private String tokenFor(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(30, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }
}
