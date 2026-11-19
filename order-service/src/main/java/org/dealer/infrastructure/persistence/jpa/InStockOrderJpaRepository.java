package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.InStockOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface InStockOrderJpaRepository extends JpaRepository<InStockOrderEntity, UUID> {
    List<InStockOrderEntity> findByCustomer_Id(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from InStockOrderEntity e where e.id = :id")
    Optional<InStockOrderEntity> findForUpdate(@Param("id") UUID id);
}
