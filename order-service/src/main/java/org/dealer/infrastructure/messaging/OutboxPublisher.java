package org.dealer.infrastructure.messaging;

import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.infrastructure.persistence.entity.OutboxEventEntity;
import org.dealer.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "dealer.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(
            OutboxEventJpaRepository outboxEventJpaRepository,
            RabbitTemplate rabbitTemplate
    ) {
        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> events = outboxEventJpaRepository
                .findTop20ByStatusInOrderByCreatedAtAsc(List.of(
                        OutboxEventStatus.NEW,
                        OutboxEventStatus.FAILED
                ));

        for (OutboxEventEntity event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEventEntity event) {
        try {
            CorrelationData correlation = new CorrelationData(event.getId().toString());
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EXCHANGE,
                    RabbitMqConfig.ORDER_SENT_FOR_APPROVAL_ROUTING_KEY,
                    event.getPayload(),
                    message -> {
                        message.getMessageProperties().setMessageId(event.getId().toString());
                        return message;
                    },
                    correlation
            );

            CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ nack: " + confirm.getReason());
            }
            if (correlation.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ returned: "
                        + correlation.getReturned().getReplyText());
            }

            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setErrorMessage(null);
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Outbox publication failed, eventId={}", event.getId(), exception);
            event.setStatus(OutboxEventStatus.FAILED);
            event.setPublishedAt(null);
            event.setErrorMessage(exception.toString());
        }

        outboxEventJpaRepository.save(event);
    }
}
