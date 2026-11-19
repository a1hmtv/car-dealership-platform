package org.dealer.application.outbox;

import java.util.UUID;

public interface OutboxEventUseCase {

    void createEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            Object payload
    );
}
