package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.CustomOrderPartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomOrderPartJpaRepository extends JpaRepository<CustomOrderPartEntity, UUID> {
    List<CustomOrderPartEntity> findByCustomOrder_Id(UUID customOrderId);
    void deleteByCustomOrder_Id(UUID customOrderId);
}
