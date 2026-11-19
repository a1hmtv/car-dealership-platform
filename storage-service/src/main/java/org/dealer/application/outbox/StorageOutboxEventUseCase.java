package org.dealer.application.outbox;

import java.util.UUID;

public interface StorageOutboxEventUseCase {

    void createEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String exchange,
            String routingKey,
            Object payload
    );
}
