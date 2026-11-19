package org.dealer.infrastructure.persistence.jpa;

import org.dealer.infrastructure.persistence.entity.PartCompatibleCarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PartCompatibleCarModelJpaRepository extends JpaRepository<PartCompatibleCarModelEntity, UUID> {
    List<PartCompatibleCarModelEntity> findByPart_Id(UUID partId);
    void deleteByPart_Id(UUID partId);
}
