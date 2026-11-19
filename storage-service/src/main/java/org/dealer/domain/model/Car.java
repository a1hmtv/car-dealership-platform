package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.enums.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class Car {
    private final UUID id;
    private final UUID carModelId;
    private final String vin;
    private final String color;
    private final BigDecimal price;
    private final BodyType bodyType;
    private final FuelType fuelType;
    private final TransmissionType transmissionType;
    private final DriveType driveType;
    private final CarStatus status;
    private final boolean availableForTestDrive;

    public Car(
            UUID id,
            UUID carModelId,
            String vin,
            String color,
            BigDecimal price,
            BodyType bodyType,
            FuelType fuelType,
            TransmissionType transmissionType,
            DriveType driveType,
            CarStatus status,
            boolean availableForTestDrive
    ) {
        this.id = id;
        this.carModelId = carModelId;
        this.vin = vin;
        this.color = color;
        this.price = price;
        this.bodyType = bodyType;
        this.fuelType = fuelType;
        this.transmissionType = transmissionType;
        this.driveType = driveType;
        this.status = status;
        this.availableForTestDrive = availableForTestDrive;
    }
}