package org.dealer.presentation.dto.configurator;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class BuildConfigurationRequestDto {
    public UUID modelId;
    public Map<String, UUID> selectedOptions;
}
