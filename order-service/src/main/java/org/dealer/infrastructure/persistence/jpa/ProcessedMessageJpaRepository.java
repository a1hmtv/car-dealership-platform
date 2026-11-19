package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedMessageJpaRepository extends JpaRepository<ProcessedMessageEntity, UUID> {
}
