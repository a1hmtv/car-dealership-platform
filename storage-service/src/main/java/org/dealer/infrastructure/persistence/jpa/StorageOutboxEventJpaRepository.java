package org.dealer.infrastructure.persistence.jpa;

import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.infrastructure.persistence.entity.StorageOutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StorageOutboxEventJpaRepository extends JpaRepository<StorageOutboxEventEntity, UUID> {

    List<StorageOutboxEventEntity> findTop20ByStatusInOrderByCreatedAtAsc(List<OutboxEventStatus> statuses);
}
