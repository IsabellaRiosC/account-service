package com.nttdata.accountservice.api.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountOperationRequestDto {

    private BigDecimal amount;
    private String operationType;
    private String operationId;
}
