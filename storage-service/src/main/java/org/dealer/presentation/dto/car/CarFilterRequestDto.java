package org.dealer.presentation.dto.car;

import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.enums.BodyType;
import org.dealer.domain.enums.DriveType;
import org.dealer.domain.enums.FuelType;
import org.dealer.domain.enums.TransmissionType;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CarFilterRequestDto {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private UUID modelId;
    private String color;
    private Integer enginePower;
    private Double engineVolume;
    private BodyType body;
    private DriveType drive;
    private FuelType fuel;
    private TransmissionType transmission;
}
