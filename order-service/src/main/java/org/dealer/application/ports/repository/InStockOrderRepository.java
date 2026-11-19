package org.dealer.application.ports.repository;

import org.dealer.domain.model.InStockOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockOrderRepository {
    Optional<InStockOrder> findByID(UUID id);

    Optional<InStockOrder> findByIDForUpdate(UUID id);
    List<InStockOrder> findAll();
    List<InStockOrder> findByCustomerId(UUID customerId);
    InStockOrder save(InStockOrder order);
    void deleteByID(UUID id);
}
