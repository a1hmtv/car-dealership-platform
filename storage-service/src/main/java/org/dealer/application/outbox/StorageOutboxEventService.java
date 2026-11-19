package org.dealer.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.infrastructure.persistence.entity.StorageOutboxEventEntity;
import org.dealer.infrastructure.persistence.jpa.StorageOutboxEventJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StorageOutboxEventService implements StorageOutboxEventUseCase {

    private final StorageOutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public StorageOutboxEventService(
            StorageOutboxEventJpaRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void createEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String exchange,
            String routingKey,
            Object payload
    ) {
        StorageOutboxEventEntity event = new StorageOutboxEventEntity();
        event.setAggregateId(aggregateId.toString());
        event.setAggregateType(aggregateType);
        event.setEventType(eventType);
        event.setExchangeName(exchange);
        event.setRoutingKey(routingKey);
        event.setPayload(toJson(payload));
        event.setStatus(OutboxEventStatus.NEW);

        repository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("Failed to serialize storage outbox payload");
        }
    }
}
