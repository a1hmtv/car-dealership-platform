package org.dealer.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.infrastructure.persistence.entity.OutboxEventEntity;
import org.dealer.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxEventService implements OutboxEventUseCase {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(
            OutboxEventJpaRepository outboxEventJpaRepository,
            ObjectMapper objectMapper
    ) {
        if (outboxEventJpaRepository == null) {
            throw new DomainValidationException("OutboxEventJpaRepository must be not null");
        }
        if (objectMapper == null) {
            throw new DomainValidationException("ObjectMapper must be not null");
        }

        this.outboxEventJpaRepository = outboxEventJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void createEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            Object payload
    ) {
        if (aggregateId == null) {
            throw new DomainValidationException("AggregateId must be not null");
        }
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new DomainValidationException("AggregateType must be not blank");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new DomainValidationException("EventType must be not blank");
        }
        if (payload == null) {
            throw new DomainValidationException("Payload must be not null");
        }

        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateId(aggregateId.toString());
        event.setAggregateType(aggregateType);
        event.setEventType(eventType);
        event.setPayload(toJson(payload));
        event.setStatus(OutboxEventStatus.NEW);

        outboxEventJpaRepository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("Failed to serialize outbox payload");
        }
    }
}
