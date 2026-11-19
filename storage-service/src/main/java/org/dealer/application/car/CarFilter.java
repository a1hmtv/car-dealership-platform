package org.dealer.application.car;

import lombok.Getter;
import lombok.Builder;
import org.dealer.domain.enums.BodyType;
import org.dealer.domain.enums.DriveType;
import org.dealer.domain.enums.FuelType;
import org.dealer.domain.enums.TransmissionType;

import java.util.UUID;

@Getter
@Builder

public class CarFilter {
    private final Integer minPrice;
    private final Integer maxPrice;
    private final String brand;
    private final UUID modelID;
    private final String color;
    private final Integer enginePower;
    private final Double engineVolume;
    private final BodyType body;
    private final DriveType drive;
    private final FuelType fuel;
    private final TransmissionType transmission;
}
