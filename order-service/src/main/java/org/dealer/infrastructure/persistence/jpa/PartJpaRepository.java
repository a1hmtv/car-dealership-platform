package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.PartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartJpaRepository extends JpaRepository<PartEntity, UUID> {
}