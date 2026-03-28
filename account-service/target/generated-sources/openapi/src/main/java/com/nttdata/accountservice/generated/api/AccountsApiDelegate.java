package com.nttdata.accountservice.generated.api;

import com.nttdata.accountservice.generated.model.AccountResponse;
import com.nttdata.accountservice.generated.model.BalanceOperationRequest;
import com.nttdata.accountservice.generated.model.CreateAccountRequest;
import com.nttdata.accountservice.generated.model.UpdateAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.codec.multipart.Part;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link AccountsApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T20:37:50.755963500-05:00[America/Lima]", comments = "Generator version: 7.5.0")
public interface AccountsApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * POST /api/accounts : Create account
     *
     * @param createAccountRequest  (required)
     * @return Account created (status code 201)
     * @see AccountsApi#createAccount
     */
    default Mono<ResponseEntity<AccountResponse>> createAccount(Mono<CreateAccountRequest> createAccountRequest,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "{ \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" }";
                result = ApiUtil.getExampleResponse(exchange, MediaType.valueOf("application/json"), exampleString);
                break;
            }
        }
        return result.then(createAccountRequest).then(Mono.empty());

    }

    /**
     * DELETE /api/accounts/{accountId} : Delete account
     *
     * @param accountId  (required)
     * @return Account deleted (status code 204)
     * @see AccountsApi#deleteAccount
     */
    default Mono<ResponseEntity<Void>> deleteAccount(String accountId,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        return result.then(Mono.empty());

    }

    /**
     * GET /api/accounts/{accountId} : Get account by id
     *
     * @param accountId  (required)
     * @return Account found (status code 200)
     * @see AccountsApi#getAccountById
     */
    default Mono<ResponseEntity<AccountResponse>> getAccountById(String accountId,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "{ \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" }";
                result = ApiUtil.getExampleResponse(exchange, MediaType.valueOf("application/json"), exampleString);
                break;
            }
        }
        return result.then(Mono.empty());

    }

    /**
     * GET /api/accounts : List accounts
     *
     * @param customerId  (optional)
     * @return Account list (status code 200)
     * @see AccountsApi#getAccounts
     */
    default Mono<ResponseEntity<Flux<AccountResponse>>> getAccounts(String customerId,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "[ { \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" } ]";
                result = ApiUtil.getExampleResponse(exchange, MediaType.valueOf("application/json"), exampleString);
                break;
            }
        }
        return result.then(Mono.empty());

    }

    /**
     * POST /api/accounts/{accountId}/operations : Apply balance operation
     *
     * @param accountId  (required)
     * @param balanceOperationRequest  (required)
     * @return Operation applied (status code 200)
     * @see AccountsApi#operateBalance
     */
    default Mono<ResponseEntity<AccountResponse>> operateBalance(String accountId,
        Mono<BalanceOperationRequest> balanceOperationRequest,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "{ \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" }";
                result = ApiUtil.getExampleResponse(exchange, MediaType.valueOf("application/json"), exampleString);
                break;
            }
        }
        return result.then(balanceOperationRequest).then(Mono.empty());

    }

    /**
     * PUT /api/accounts/{accountId} : Update account
     *
     * @param accountId  (required)
     * @param updateAccountRequest  (required)
     * @return Account updated (status code 200)
     * @see AccountsApi#updateAccount
     */
    default Mono<ResponseEntity<AccountResponse>> updateAccount(String accountId,
        Mono<UpdateAccountRequest> updateAccountRequest,
        ServerWebExchange exchange) {
        Mono<Void> result = Mono.empty();
        exchange.getResponse().setStatusCode(HttpStatus.NOT_IMPLEMENTED);
        for (MediaType mediaType : exchange.getRequest().getHeaders().getAccept()) {
            if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                String exampleString = "{ \"accountId\" : \"accountId\", \"createdAt\" : \"2000-01-23T04:56:07.000+00:00\", \"balance\" : 0.8008281904610115, \"createdBy\" : \"createdBy\", \"accountType\" : \"accountType\", \"customerId\" : \"customerId\", \"currency\" : \"currency\", \"accountNumber\" : \"accountNumber\", \"status\" : \"status\", \"updatedAt\" : \"2000-01-23T04:56:07.000+00:00\" }";
                result = ApiUtil.getExampleResponse(exchange, MediaType.valueOf("application/json"), exampleString);
                break;
            }
        }
        return result.then(updateAccountRequest).then(Mono.empty());

    }

}
