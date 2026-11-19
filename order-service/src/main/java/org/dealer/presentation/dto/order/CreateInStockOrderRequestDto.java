package org.dealer.presentation.dto.order;

import java.util.UUID;

public class CreateInStockOrderRequestDto {
    private UUID carId;

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }
}
