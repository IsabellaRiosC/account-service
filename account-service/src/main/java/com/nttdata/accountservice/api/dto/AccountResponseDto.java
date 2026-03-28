package com.nttdata.accountservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class AccountResponseDto {

    private String accountId;
    private String customerId;
    private String accountNumber;
    private String accountType;
    private String currency;
    private String status;
    private BigDecimal balance;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
