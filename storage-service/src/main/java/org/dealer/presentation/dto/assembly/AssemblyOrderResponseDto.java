package org.dealer.presentation.dto.assembly;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AssemblyOrderResponseDto {

    private UUID id;
    private UUID sourceOrderId;
    private String sourceOrderType;
    private UUID carId;
    private UUID modelId;
    private List<UUID> requiredPartIds;
    private UUID warehouseEmployeeId;
    private String traceId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean removed;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
