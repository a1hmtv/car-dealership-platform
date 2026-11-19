package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestDriveRequestJpaRepository extends JpaRepository<TestDriveRequestEntity, UUID> {
}