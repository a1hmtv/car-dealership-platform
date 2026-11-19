package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.AssemblyOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface AssemblyOrderJpaRepository extends JpaRepository<AssemblyOrderEntity, UUID> {

    List<AssemblyOrderEntity> findByRemovedFalse();

    List<AssemblyOrderEntity> findBySourceOrderId(UUID sourceOrderId);

    boolean existsBySourceOrderId(UUID sourceOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from AssemblyOrderEntity e where e.id = :id")
    Optional<AssemblyOrderEntity> findForUpdate(@Param("id") UUID id);
}
