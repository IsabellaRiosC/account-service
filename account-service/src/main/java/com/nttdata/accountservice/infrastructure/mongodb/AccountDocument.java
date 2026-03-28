package com.nttdata.accountservice.infrastructure.mongodb;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class AccountDocument {

    @Id
    private String accountId;

    @Indexed
    private String customerId;

    @Indexed(unique = true)
    private String accountNumber;

    private String accountType;
    private String currency;
    private String status;
    private BigDecimal balance;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
