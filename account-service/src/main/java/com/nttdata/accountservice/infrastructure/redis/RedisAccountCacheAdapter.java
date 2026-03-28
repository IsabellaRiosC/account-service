package com.nttdata.accountservice.infrastructure.redis;

import com.nttdata.accountservice.domain.port.AccountCachePort;
import java.math.BigDecimal;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisAccountCacheAdapter implements AccountCachePort {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<BigDecimal> getBalance(String accountId) {
        return redisTemplate.opsForValue()
                .get(balanceKey(accountId))
                .map(BigDecimal::new)
                .onErrorReturn(BigDecimal.ZERO);
    }

    @Override
    public Mono<Void> putBalance(String accountId, BigDecimal balance) {
        return redisTemplate.opsForValue()
                .set(balanceKey(accountId), balance.toPlainString(), Duration.ofMinutes(15))
                .then();
    }

    @Override
    public Mono<Void> evictBalance(String accountId) {
        return redisTemplate.delete(balanceKey(accountId)).then();
    }

    @Override
    public Mono<Boolean> registerOperation(String operationId) {
        return redisTemplate.opsForValue()
                .setIfAbsent(operationKey(operationId), "1", Duration.ofHours(1))
                .onErrorReturn(true);
    }

    private String balanceKey(String accountId) {
        return "account:balance:" + accountId;
    }

    private String operationKey(String operationId) {
        return "account:operation:" + operationId;
    }
}
