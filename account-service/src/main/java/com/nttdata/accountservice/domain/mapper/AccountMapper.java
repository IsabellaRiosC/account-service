package com.nttdata.accountservice.domain.mapper;

import com.nttdata.accountservice.api.dto.AccountCreateRequestDto;
import com.nttdata.accountservice.api.dto.AccountOperationRequestDto;
import com.nttdata.accountservice.api.dto.AccountResponseDto;
import com.nttdata.accountservice.api.dto.AccountUpdateRequestDto;
import com.nttdata.accountservice.domain.port.AccountRecord;
import com.nttdata.accountservice.generated.model.AccountResponse;
import com.nttdata.accountservice.generated.model.BalanceOperationRequest;
import com.nttdata.accountservice.generated.model.CreateAccountRequest;
import com.nttdata.accountservice.generated.model.UpdateAccountRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountRecord toNewRecord(AccountCreateRequestDto request, String requestedBy) {
        Instant now = Instant.now();
        BigDecimal initialBalance = request.getInitialBalance() == null ? BigDecimal.ZERO : request.getInitialBalance();
        return new AccountRecord(
                "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                request.getCustomerId(),
                request.getAccountNumber(),
                request.getAccountType(),
                request.getCurrency(),
                "ACTIVE",
                initialBalance,
                requestedBy,
                now,
                now);
    }

    public static AccountRecord mergeStatus(AccountRecord current, AccountUpdateRequestDto request) {
        return new AccountRecord(
                current.accountId(),
                current.customerId(),
                current.accountNumber(),
                current.accountType(),
                current.currency(),
                request.getStatus(),
                current.balance(),
                current.createdBy(),
                current.createdAt(),
                Instant.now());
    }

    public static AccountRecord applyOperation(AccountRecord current, AccountOperationRequestDto request) {
        BigDecimal amount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
        BigDecimal nextBalance = "DEBIT".equalsIgnoreCase(request.getOperationType())
                ? current.balance().subtract(amount)
                : current.balance().add(amount);

        return new AccountRecord(
                current.accountId(),
                current.customerId(),
                current.accountNumber(),
                current.accountType(),
                current.currency(),
                current.status(),
                nextBalance,
                current.createdBy(),
                current.createdAt(),
                Instant.now());
    }

    public static AccountResponseDto toDto(AccountRecord record) {
        AccountResponseDto dto = new AccountResponseDto();
        dto.setAccountId(record.accountId());
        dto.setCustomerId(record.customerId());
        dto.setAccountNumber(record.accountNumber());
        dto.setAccountType(record.accountType());
        dto.setCurrency(record.currency());
        dto.setStatus(record.status());
        dto.setBalance(record.balance());
        dto.setCreatedBy(record.createdBy());
        dto.setCreatedAt(record.createdAt());
        dto.setUpdatedAt(record.updatedAt());
        return dto;
    }

    public static AccountResponse toGenerated(AccountResponseDto dto) {
        AccountResponse model = new AccountResponse();
        model.setAccountId(dto.getAccountId());
        model.setCustomerId(dto.getCustomerId());
        model.setAccountNumber(dto.getAccountNumber());
        model.setAccountType(dto.getAccountType());
        model.setCurrency(dto.getCurrency());
        model.setStatus(dto.getStatus());
        model.setBalance(dto.getBalance() == null ? null : dto.getBalance().doubleValue());
        model.setCreatedBy(dto.getCreatedBy());
        model.setCreatedAt(dto.getCreatedAt() == null ? null : OffsetDateTime.ofInstant(dto.getCreatedAt(),
            java.time.ZoneOffset.UTC));
        model.setUpdatedAt(dto.getUpdatedAt() == null ? null : OffsetDateTime.ofInstant(dto.getUpdatedAt(),
            java.time.ZoneOffset.UTC));
        return model;
    }

    public static AccountCreateRequestDto toCreateDto(CreateAccountRequest request) {
        AccountCreateRequestDto dto = new AccountCreateRequestDto();
        dto.setCustomerId(request.getCustomerId());
        dto.setAccountNumber(request.getAccountNumber());
        dto.setAccountType(String.valueOf(request.getAccountType()));
        dto.setCurrency(request.getCurrency());
        dto.setInitialBalance(request.getInitialBalance() == null ? null : BigDecimal.valueOf(request.getInitialBalance()));
        return dto;
    }

    public static AccountUpdateRequestDto toUpdateDto(UpdateAccountRequest request) {
        AccountUpdateRequestDto dto = new AccountUpdateRequestDto();
        dto.setStatus(String.valueOf(request.getStatus()));
        return dto;
    }

    public static AccountOperationRequestDto toOperationDto(BalanceOperationRequest request) {
        AccountOperationRequestDto dto = new AccountOperationRequestDto();
        dto.setOperationType(String.valueOf(request.getOperationType()));
        dto.setOperationId(request.getOperationId());
        dto.setAmount(request.getAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(request.getAmount()));
        return dto;
    }
}
