package org.dealer.presentation.dto.assembly;

import java.util.List;
import java.util.UUID;

public class CreateAssemblyOrderRequestDto {

    private UUID sourceOrderId;
    private String sourceOrderType;
    private UUID carId;
    private UUID modelId;
    private List<UUID> requiredPartIds;
    private UUID warehouseEmployeeId;

    public UUID getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(UUID sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderType() {
        return sourceOrderType;
    }

    public void setSourceOrderType(String sourceOrderType) {
        this.sourceOrderType = sourceOrderType;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public UUID getModelId() {
        return modelId;
    }

    public void setModelId(UUID modelId) {
        this.modelId = modelId;
    }

    public List<UUID> getRequiredPartIds() {
        return requiredPartIds;
    }

    public void setRequiredPartIds(List<UUID> requiredPartIds) {
        this.requiredPartIds = requiredPartIds;
    }

    public UUID getWarehouseEmployeeId() {
        return warehouseEmployeeId;
    }

    public void setWarehouseEmployeeId(UUID warehouseEmployeeId) {
        this.warehouseEmployeeId = warehouseEmployeeId;
    }
}
