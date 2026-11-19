package org.dealer.presentation.dto.assembly;

import java.util.UUID;

public class UpdateAssemblyOrderRequestDto {

    private String status;
    private UUID warehouseEmployeeId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getWarehouseEmployeeId() {
        return warehouseEmployeeId;
    }

    public void setWarehouseEmployeeId(UUID warehouseEmployeeId) {
        this.warehouseEmployeeId = warehouseEmployeeId;
    }
}
