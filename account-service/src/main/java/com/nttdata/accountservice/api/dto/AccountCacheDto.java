package com.nttdata.accountservice.api.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountCacheDto {

    String accountId;
    BigDecimal balance;
}
