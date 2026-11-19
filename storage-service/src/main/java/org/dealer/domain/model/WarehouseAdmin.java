package org.dealer.domain.model;

import java.util.UUID;

public class WarehouseAdmin {
    private final UUID id;
    private final String fullName;

    public WarehouseAdmin(UUID id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}