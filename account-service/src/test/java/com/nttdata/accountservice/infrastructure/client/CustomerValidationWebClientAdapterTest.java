package com.nttdata.accountservice.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CustomerValidationWebClientAdapterTest {

    @Test
    void existsCustomerShouldPropagateAuthenticatedUserHeader() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        CustomerValidationWebClientAdapter adapter = adapterWith(request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        });

        StepVerifier.create(adapter.existsCustomer("CUS-1", "demo.user"))
                .expectNext(true)
                .verifyComplete();

        assertThat(capturedRequest.get()).isNotNull();
        assertThat(capturedRequest.get().headers().getFirst("X-Auth-User")).isEqualTo("demo.user");
    }

    @Test
    void existsCustomerShouldReturnFalseWhenCustomerServiceReturnsNotFound() {
        CustomerValidationWebClientAdapter adapter = adapterWith(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));

        StepVerifier.create(adapter.existsCustomer("CUS-404", "demo.user"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsCustomerShouldSurfaceUnauthorizedAsBadGateway() {
        CustomerValidationWebClientAdapter adapter = adapterWith(request ->
                Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));

        StepVerifier.create(adapter.existsCustomer("CUS-401", "demo.user"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(exception.getReason()).contains("rejected authentication");
                })
                .verify();
    }

    private CustomerValidationWebClientAdapter adapterWith(ExchangeFunction exchangeFunction) {
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        CustomerValidationWebClientAdapter adapter = new CustomerValidationWebClientAdapter(
                builder,
                CircuitBreakerRegistry.ofDefaults());
        ReflectionTestUtils.setField(adapter, "customerServiceBaseUrl", "http://localhost:7002");
        return adapter;
    }
}

