package com.nttdata.accountservice.api.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountCreateRequestDto {

    private String customerId;
    private String accountNumber;
    private String accountType;
    private String currency;
    private BigDecimal initialBalance;
}
