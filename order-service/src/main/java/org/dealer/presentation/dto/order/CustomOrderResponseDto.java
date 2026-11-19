package org.dealer.presentation.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CustomOrderResponseDto {
    private UUID orderId;
    private UUID customerId;
    private UUID managerId;
    private UUID modelId;
    private String status;
    private BigDecimal totalPrice;
    private Map<String, UUID> selectedOptions;
}
