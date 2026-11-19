package org.dealer.application.ports.repository;

import org.dealer.domain.model.CustomOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomOrderRepository {
    Optional<CustomOrder> findByID(UUID id);

    Optional<CustomOrder> findByIDForUpdate(UUID id);
    List<CustomOrder> findAll();
    List<CustomOrder> findByCustomerId(UUID customerId);
    CustomOrder save(CustomOrder order);
    void deleteByID(UUID id);
}
