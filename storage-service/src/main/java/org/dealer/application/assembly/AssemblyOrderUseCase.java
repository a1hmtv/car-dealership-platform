package org.dealer.application.assembly;

import org.dealer.domain.enums.AssemblyOrderStatus;
import org.dealer.domain.model.AssemblyOrder;

import java.util.List;
import java.util.UUID;

public interface AssemblyOrderUseCase {

    AssemblyOrder create(
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID modelId,
            List<UUID> requiredPartIds,
            UUID warehouseEmployeeId,
            String traceId
    );

    AssemblyOrder getById(UUID id);

    List<AssemblyOrder> findAll();

    AssemblyOrder updateStatus(UUID id, AssemblyOrderStatus status);

    AssemblyOrder assignWarehouseEmployee(UUID id, UUID warehouseEmployeeId);

    void delete(UUID id);
}
