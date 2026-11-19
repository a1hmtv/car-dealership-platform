package org.dealer.infrastructure.persistence.jpa;

import org.dealer.domain.enums.CarStatus;
import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarJpaRepository extends JpaRepository<CarEntity, UUID>, JpaSpecificationExecutor<CarEntity> {

    List<CarEntity> findAllByStatus(CarStatus status);

    Optional<CarEntity> findByIdAndStatus(UUID id, CarStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CarEntity e where e.id = :id")
    Optional<CarEntity> findForUpdate(@Param("id") UUID id);
}
