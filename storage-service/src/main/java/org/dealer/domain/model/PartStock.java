package org.dealer.domain.model;

import org.dealer.domain.exceptions.DomainValidationException;

import java.util.UUID;

public class PartStock {

    private final UUID id;
    private final UUID partId;
    private int totalQuantity;
    private int reservedQuantity;
    private boolean removed;

    public PartStock(
            UUID id,
            UUID partId,
            int totalQuantity,
            int reservedQuantity,
            boolean removed
    ) {
        if (id == null) {
            throw new DomainValidationException("PartStock id must be not null");
        }
        if (partId == null) {
            throw new DomainValidationException("PartId must be not null");
        }
        if (totalQuantity < 0) {
            throw new DomainValidationException("Total quantity must be non-negative");
        }
        if (reservedQuantity < 0) {
            throw new DomainValidationException("Reserved quantity must be non-negative");
        }
        if (reservedQuantity > totalQuantity) {
            throw new DomainValidationException("Reserved quantity cannot be greater than total quantity");
        }

        this.id = id;
        this.partId = partId;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.removed = removed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPartId() {
        return partId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public boolean isRemoved() {
        return removed;
    }

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }

    public boolean canReserve(int quantity) {
        return !removed && quantity > 0 && getAvailableQuantity() >= quantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new DomainValidationException("Not enough parts in stock: " + partId);
        }

        reservedQuantity += quantity;
    }
    public void release(int quantity) {
        validateReservedQuantity(quantity);
        reservedQuantity -= quantity;
    }

    public void consume(int quantity) {
        validateReservedQuantity(quantity);
        reservedQuantity -= quantity;
        totalQuantity -= quantity;
    }

    private void validateReservedQuantity(int quantity) {
        if (removed || quantity <= 0 || quantity > reservedQuantity) {
            throw new DomainValidationException("Invalid reserved quantity for part: " + partId);
        }
    }
}
