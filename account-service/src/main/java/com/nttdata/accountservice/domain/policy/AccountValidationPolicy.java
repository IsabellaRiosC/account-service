package com.nttdata.accountservice.domain.policy;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.api.dto.AccountUpdateRequestDto;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AccountValidationPolicy {

    public void validateRequestedBy(String requestedBy) {
        if (requestedBy == null || requestedBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
        }
    }

    public void validateCreate(AccountCreateRequestDto request) {
        if (request == null
                || isBlank(request.getCustomerId())
                || isBlank(request.getAccountNumber())
                || isBlank(request.getAccountType())
                || isBlank(request.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid create account payload");
        }
    }

    public void validateUpdate(String accountId, AccountUpdateRequestDto request) {
        validateAccountId(accountId);
        if (request == null || isBlank(request.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid update account payload");
        }
    }

    public void validateOperation(String accountId, AccountOperationRequestDto request) {
        validateAccountId(accountId);
        if (request == null
                || isBlank(request.getOperationType())
                || isBlank(request.getOperationId())
                || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation payload");
        }
    }

    public void validateAccountId(String accountId) {
        if (isBlank(accountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account id");
        }
    }

    public void validateCustomerExists(boolean customerExists, String customerId) {
        if (!customerExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found: " + customerId);
        }
    }

    public void validateSufficientBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
