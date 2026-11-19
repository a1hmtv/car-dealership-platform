package org.dealer.presentation.dto.car;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CarResponseDto {

    private UUID id;
    private UUID carModelId;
    private String vin;
    private String color;
    private BigDecimal price;
    private String bodyType;
    private String fuelType;
    private String transmissionType;
    private String driveType;
    private String status;
    private boolean availableForTestDrive;

}
