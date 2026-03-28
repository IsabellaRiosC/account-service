package com.nttdata.accountservice.domain.port;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountRecord(
        String accountId,
        String customerId,
        String accountNumber,
        String accountType,
        String currency,
        String status,
        BigDecimal balance,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {
}
