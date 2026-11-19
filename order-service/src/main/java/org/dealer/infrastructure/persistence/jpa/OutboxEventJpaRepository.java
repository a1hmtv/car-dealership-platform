package org.dealer.infrastructure.persistence.jpa;

import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    List<OutboxEventEntity> findTop20ByStatusInOrderByCreatedAtAsc(List<OutboxEventStatus> statuses);
}
