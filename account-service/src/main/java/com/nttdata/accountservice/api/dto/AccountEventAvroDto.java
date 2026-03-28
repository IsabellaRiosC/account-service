package com.nttdata.accountservice.api.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountEventAvroDto {

    String eventType;
    String accountId;
    String customerId;
    BigDecimal balance;
    String operationId;
    String status;
}
