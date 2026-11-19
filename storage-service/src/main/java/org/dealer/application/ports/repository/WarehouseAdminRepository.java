package org.dealer.application.ports.repository;

import org.dealer.domain.model.WarehouseAdmin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseAdminRepository {

    WarehouseAdmin save(WarehouseAdmin warehouseAdmin);
    Optional<WarehouseAdmin> findById(UUID id);
    List<WarehouseAdmin> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
