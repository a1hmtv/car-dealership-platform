package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarModelJpaRepository extends JpaRepository<CarModelEntity, UUID> {
}