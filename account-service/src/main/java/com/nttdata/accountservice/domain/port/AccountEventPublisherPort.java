package com.nttdata.accountservice.domain.port;

import com.nttdata.accountservice.api.dto.AccountEventAvroDto;
import reactor.core.publisher.Mono;

public interface AccountEventPublisherPort {

    Mono<Void> publish(AccountEventAvroDto event);
}
