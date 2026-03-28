package com.nttdata.accountservice.infrastructure.client;

import com.nttdata.accountservice.domain.port.CustomerValidationPort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerValidationWebClientAdapter implements CustomerValidationPort {

    private static final String AUTHENTICATED_USER_HEADER = "X-Auth-User";

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${account.customer-service.base-url}")
    private String customerServiceBaseUrl;

    @Override
    public Mono<Boolean> existsCustomer(String customerId, String requestedBy) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("customerValidation");

        return webClientBuilder
                .baseUrl(customerServiceBaseUrl)
                .build()
                .get()
                .uri("/api/customers/{customerId}", customerId)
                .header(AUTHENTICATED_USER_HEADER, requestedBy)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.releaseBody().thenReturn(true);
                    }
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return response.releaseBody().thenReturn(false);
                    }
                    if (response.statusCode() == HttpStatus.UNAUTHORIZED
                            || response.statusCode() == HttpStatus.FORBIDDEN) {
                        return response.releaseBody().then(Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "Customer service rejected authentication for customerId=" + customerId)));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .doOnError(ex -> log.error("Customer validation failed customerId={}", customerId, ex))
                .onErrorResume(ResponseStatusException.class,
                        ex -> ex.getStatusCode() == HttpStatus.NOT_FOUND ? Mono.just(false) : Mono.error(ex));
    }
}
