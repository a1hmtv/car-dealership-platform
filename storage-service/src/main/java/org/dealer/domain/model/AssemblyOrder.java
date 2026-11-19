package org.dealer.domain.model;

import org.dealer.domain.enums.AssemblyOrderStatus;
import org.dealer.domain.exceptions.DomainValidationException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AssemblyOrder {

    private final UUID id;
    private final UUID sourceOrderId;
    private final String sourceOrderType;
    private final UUID carId;
    private final UUID modelId;
    private final List<UUID> requiredPartIds;
    private final UUID warehouseEmployeeId;
    private final String traceId;
    private AssemblyOrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean removed;

    public AssemblyOrder(
            UUID id,
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID modelId,
            List<UUID> requiredPartIds,
            UUID warehouseEmployeeId,
            String traceId,
            AssemblyOrderStatus status,
            Instant createdAt,
            Instant updatedAt,
            boolean removed
    ) {
        if (id == null) {
            throw new DomainValidationException("AssemblyOrder id must be not null");
        }
        if (sourceOrderId == null) {
            throw new DomainValidationException("SourceOrderId must be not null");
        }
        if (sourceOrderType == null || sourceOrderType.isBlank()) {
            throw new DomainValidationException("SourceOrderType must be not blank");
        }
        if (status == null) {
            throw new DomainValidationException("AssemblyOrder status must be not null");
        }

        this.id = id;
        this.sourceOrderId = sourceOrderId;
        this.sourceOrderType = sourceOrderType;
        this.carId = carId;
        this.modelId = modelId;
        this.requiredPartIds = requiredPartIds == null ? List.of() : List.copyOf(requiredPartIds);
        this.warehouseEmployeeId = warehouseEmployeeId;
        this.traceId = traceId;
        this.status = status;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        this.removed = removed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceOrderId() {
        return sourceOrderId;
    }

    public String getSourceOrderType() {
        return sourceOrderType;
    }

    public UUID getCarId() {
        return carId;
    }

    public UUID getModelId() {
        return modelId;
    }

    public List<UUID> getRequiredPartIds() {
        return requiredPartIds;
    }

    public UUID getWarehouseEmployeeId() {
        return warehouseEmployeeId;
    }

    public String getTraceId() {
        return traceId;
    }

    public AssemblyOrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void changeStatus(AssemblyOrderStatus newStatus) {
        if (newStatus == null) {
            throw new DomainValidationException("New status must be not null");
        }
        if (removed) {
            throw new DomainValidationException("Removed assembly order cannot be changed");
        }

        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void markRemoved() {
        this.removed = true;
        this.updatedAt = Instant.now();
    }
}
