package com.nttdata.accountservice.api;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", ex.getStatusCode().value());
        errorBody.put("error", getReasonPhrase(ex.getStatusCode()));
        errorBody.put("message", ex.getReason() != null ? ex.getReason() : ex.getMessage());
        errorBody.put("path", exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorBody));
    }

    private String getReasonPhrase(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus) {
            return ((HttpStatus) statusCode).getReasonPhrase();
        }
        return statusCode.toString();
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorBody.put("message", ex.getMessage());
        errorBody.put("path", exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody));
    }
}
