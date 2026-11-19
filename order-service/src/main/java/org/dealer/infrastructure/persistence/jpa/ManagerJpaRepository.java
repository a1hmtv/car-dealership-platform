package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagerJpaRepository extends JpaRepository<ManagerEntity, UUID> {
}