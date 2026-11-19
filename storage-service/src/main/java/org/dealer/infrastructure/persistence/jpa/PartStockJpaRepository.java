package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.PartStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface PartStockJpaRepository extends JpaRepository<PartStockEntity, UUID> {

    Optional<PartStockEntity> findByPartIdAndRemovedFalse(UUID partId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PartStockEntity e where e.partId = :id and e.removed = false")
    Optional<PartStockEntity> findForUpdate(@Param("id") UUID id);
}
