package org.dealer.presentation.controller;

import org.dealer.application.configurator.ConfiguratorUseCase;
import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.presentation.dto.configurator.BuildConfigurationRequestDto;
import org.dealer.presentation.dto.configurator.ConfigurationResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/configurator")
public class ConfiguratorController {

    private final ConfiguratorUseCase configuratorUseCase;

    public ConfiguratorController(ConfiguratorUseCase configuratorUseCase) {
        this.configuratorUseCase = configuratorUseCase;
    }

    @GetMapping("/{modelId}/options")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Map<String, List<String>> getAvailableOptions(@PathVariable UUID modelId) {
        return configuratorUseCase.getAvailableOptions(modelId)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().stream()
                                .map(ComponentOption::getName)
                                .toList()
                ));
    }

    @PostMapping("/build")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ConfigurationResponseDto buildConfiguration(@RequestBody BuildConfigurationRequestDto request) {
        Configuration configuration = toConfiguration(request);

        ConfigurationResponseDto dto = new ConfigurationResponseDto();
        dto.setSelectedOptions(toSelectedOptionsMap(configuration));
        dto.setTotalPrice(configuratorUseCase.calculateTotalPrice(request.getModelId(), configuration));
        dto.setPriceBreakdown(toPriceBreakdownMap(
                configuratorUseCase.priceBreakdown(request.getModelId(), configuration)
        ));

        return dto;
    }

    private Configuration toConfiguration(BuildConfigurationRequestDto request) {
        if (request == null) {
            throw new DomainValidationException("Request must be not null");
        }
        if (request.getModelId() == null) {
            throw new DomainValidationException("ModelId must be not null");
        }
        if (request.getSelectedOptions() == null || request.getSelectedOptions().isEmpty()) {
            throw new DomainValidationException("Selected options must be not null or empty");
        }

        Map<ComponentType, List<ComponentOption>> availableOptions =
                configuratorUseCase.getAvailableOptions(request.getModelId());

        Map<ComponentType, ComponentOption> selected = new EnumMap<>(ComponentType.class);

        for (Map.Entry<String, UUID> entry : request.getSelectedOptions().entrySet()) {
            ComponentType type = ComponentType.valueOf(entry.getKey().toUpperCase());

            ComponentOption option = availableOptions.get(type).stream()
                    .filter(item -> item.getPartId().equals(entry.getValue()))
                    .findFirst()
                    .orElseThrow(() -> new DomainValidationException(
                            "Selected option is unavailable for component: " + type
                    ));

            selected.put(type, option);
        }

        return configuratorUseCase.buildConfiguration(request.getModelId(), selected);
    }

    private Map<String, UUID> toSelectedOptionsMap(Configuration configuration) {
        return configuration.getSelectedOptions()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().getPartId()
                ));
    }

    private Map<String, Integer> toPriceBreakdownMap(Map<ComponentType, Integer> priceBreakdown) {
        return priceBreakdown.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue
                ));
    }
}
