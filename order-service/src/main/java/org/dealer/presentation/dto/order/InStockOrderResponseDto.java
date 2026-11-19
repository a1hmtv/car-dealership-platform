package org.dealer.presentation.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class InStockOrderResponseDto {
    private UUID orderId;
    private UUID customerId;
    private UUID managerId;
    private UUID carId;
    private String status;
    private BigDecimal totalPrice;
}
