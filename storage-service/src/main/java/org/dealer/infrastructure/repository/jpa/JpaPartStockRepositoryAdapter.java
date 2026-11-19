package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.PartStockRepository;
import org.dealer.domain.model.PartStock;
import org.dealer.infrastructure.persistence.entity.PartStockEntity;
import org.dealer.infrastructure.persistence.jpa.PartStockJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPartStockRepositoryAdapter implements PartStockRepository {

    private final PartStockJpaRepository partStockJpaRepository;

    public JpaPartStockRepositoryAdapter(PartStockJpaRepository partStockJpaRepository) {
        this.partStockJpaRepository = partStockJpaRepository;
    }

    @Override
    public Optional<PartStock> findByPartIdForUpdate(UUID id) {
        return partStockJpaRepository.findForUpdate(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<PartStock> findByPartId(UUID partId) {
        return partStockJpaRepository.findByPartIdAndRemovedFalse(partId)
                .map(this::toDomain);
    }

    @Override
    public PartStock save(PartStock partStock) {
        PartStockEntity entity = partStockJpaRepository.findById(partStock.getId())
                .orElseGet(PartStockEntity::new);

        entity.setId(partStock.getId());
        entity.setPartId(partStock.getPartId());
        entity.setTotalQuantity(partStock.getTotalQuantity());
        entity.setReservedQuantity(partStock.getReservedQuantity());
        entity.setRemoved(partStock.isRemoved());

        return toDomain(partStockJpaRepository.save(entity));
    }

    private PartStock toDomain(PartStockEntity entity) {
        return new PartStock(
                entity.getId(),
                entity.getPartId(),
                entity.getTotalQuantity(),
                entity.getReservedQuantity(),
                entity.isRemoved()
        );
    }
}
