package com.nttdata.accountservice.infrastructure.kafka;

import com.nttdata.accountservice.api.dto.AccountEventAvroDto;
import com.nttdata.accountservice.domain.port.AccountEventPublisherPort;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KafkaAccountEventPublisher implements AccountEventPublisherPort {

    private final KafkaTemplate<String, AccountEventAvroDto> kafkaTemplate;

    public KafkaAccountEventPublisher(@Qualifier("accountEventKafkaTemplate")
                                      KafkaTemplate<String, AccountEventAvroDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${account.kafka.topic.account-created:account-created-events}")
    private String createdTopic;

    @Value("${account.kafka.topic.account-updated:account-updated-events}")
    private String updatedTopic;

    @Value("${account.kafka.topic.account-operation:account-operation-events}")
    private String operationTopic;

    @Override
    public Mono<Void> publish(AccountEventAvroDto event) {
        String topic = resolveTopic(event.getEventType());

        return Mono.fromFuture(send(topic, event.getAccountId(), event))
                .doOnNext(result -> log.info("Kafka event published topic={} accountId={} offset={}",
                        topic,
                        event.getAccountId(),
                        result.getRecordMetadata().offset()))
                .doOnError(ex -> log.error("Kafka publish error topic={} accountId={}", topic, event.getAccountId(), ex))
                .onErrorResume(ex -> Mono.empty())
                .then();
    }

    private CompletableFuture<SendResult<String, AccountEventAvroDto>> send(
            String topic,
            String key,
            AccountEventAvroDto payload) {
        return kafkaTemplate.send(topic, key, payload);
    }


    private String resolveTopic(String eventType) {
        if ("ACCOUNT_CREATED".equals(eventType)) {
            return createdTopic;
        }
        if ("ACCOUNT_UPDATED".equals(eventType)) {
            return updatedTopic;
        }
        return operationTopic;
    }
}
