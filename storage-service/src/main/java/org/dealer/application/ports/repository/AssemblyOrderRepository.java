package org.dealer.application.ports.repository;

import org.dealer.domain.model.AssemblyOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderRepository {

    AssemblyOrder save(AssemblyOrder assemblyOrder);

    Optional<AssemblyOrder> findById(UUID id);

    Optional<AssemblyOrder> findByIdForUpdate(UUID id);

    List<AssemblyOrder> findAll();

    List<AssemblyOrder> findActive();

    boolean existsBySourceOrderId(UUID sourceOrderId);

    void deleteById(UUID id);
}
