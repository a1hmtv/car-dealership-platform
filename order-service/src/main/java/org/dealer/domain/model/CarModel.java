package org.dealer.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.exceptions.DomainValidationException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter

public class CarModel {
    private final UUID id;
    private final String name;
    private final String brand;
    private final int basePrice;
    private final Map<ComponentType, List<ComponentOption>> availableOptions;

    public CarModel(
            UUID id,
            String name,
            String brand,
            int basePrice,
            Map<ComponentType, List<ComponentOption>> availableOptions
    ){
        if (id == null) throw new DomainValidationException("CarModelID must be not null");
        if (name == null) throw new DomainValidationException("Name must be not blank");
        if (basePrice < 0) throw new DomainValidationException("BasePrice must be >= 0");
        if (availableOptions == null) throw new DomainValidationException("AvailableOptions must be not null");

        this.id = id;
        this.name = name;
        this.brand = brand;
        this.basePrice = basePrice;
        this.availableOptions = availableOptions;
    }
}
