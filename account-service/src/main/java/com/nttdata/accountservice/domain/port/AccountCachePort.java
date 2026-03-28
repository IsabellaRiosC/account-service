package com.nttdata.accountservice.domain.port;

import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface AccountCachePort {

    Mono<BigDecimal> getBalance(String accountId);

    Mono<Void> putBalance(String accountId, BigDecimal balance);

    Mono<Void> evictBalance(String accountId);

    Mono<Boolean> registerOperation(String operationId);
}
