package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Part {
    private final UUID partId;
    private final String name;
    private final ComponentType componentType;
    private final BigDecimal priceDelta;
    private final Set<UUID> compatibleCarIds;

    public Part(UUID partId,
                String name,
                ComponentType componentType,
                BigDecimal priceDelta,
                Set<UUID> compatibleCarIds) {

        if (partId == null) throw new DomainValidationException("partId must be not null");
        if (name == null || name.isBlank()) throw new DomainValidationException("name must be not blank");
        if (componentType == null) throw new DomainValidationException("componentType must be not null");
        if (priceDelta == null) throw new DomainValidationException("priceDelta must be not null");
        if (compatibleCarIds == null) throw new DomainValidationException("compatibleCarIds must be not null");

        this.partId = partId;
        this.name = name;
        this.componentType = componentType;
        this.priceDelta = priceDelta;
        this.compatibleCarIds = new HashSet<>(compatibleCarIds);
    }

    public Set<UUID> getCompatibleCarIds() {
        return Collections.unmodifiableSet(compatibleCarIds);
    }

    public boolean isCompatibleWith(UUID carId) {
        if (carId == null) throw new DomainValidationException("carId must be not null");
        return compatibleCarIds.contains(carId);
    }
}