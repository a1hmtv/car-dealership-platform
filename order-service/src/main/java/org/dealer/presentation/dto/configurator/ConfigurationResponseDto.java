package org.dealer.presentation.dto.configurator;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ConfigurationResponseDto {
    public Map<String, UUID> selectedOptions;
    public int totalPrice;
    public Map<String, Integer> priceBreakdown;
}
