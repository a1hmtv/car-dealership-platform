package org.dealer.application.ports.repository;

import org.dealer.domain.model.PartStock;

import java.util.Optional;
import java.util.UUID;

public interface PartStockRepository {

    Optional<PartStock> findByPartId(UUID partId);

    Optional<PartStock> findByPartIdForUpdate(UUID id);

    PartStock save(PartStock partStock);
}
