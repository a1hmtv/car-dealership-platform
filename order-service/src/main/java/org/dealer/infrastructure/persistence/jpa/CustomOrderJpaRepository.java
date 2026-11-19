package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.CustomOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface CustomOrderJpaRepository extends JpaRepository<CustomOrderEntity, UUID> {
    List<CustomOrderEntity> findByCustomer_Id(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CustomOrderEntity e where e.id = :id")
    Optional<CustomOrderEntity> findForUpdate(@Param("id") UUID id);
}
