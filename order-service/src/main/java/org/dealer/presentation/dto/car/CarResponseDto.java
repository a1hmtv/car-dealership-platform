package org.dealer.presentation.dto.car;

import java.math.BigDecimal;
import java.util.UUID;

public record CarResponseDto(
        UUID id,
        UUID carModelId,
        String vin,
        String color,
        BigDecimal price,
        String bodyType,
        String fuelType,
        String transmissionType,
        String driveType,
        String status,
        boolean availableForTestDrive
) {
}