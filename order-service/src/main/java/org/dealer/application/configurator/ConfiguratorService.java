package org.dealer.application.configurator;

import lombok.RequiredArgsConstructor;
import org.dealer.application.ports.repository.CarModelRepository;
import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.exceptions.IncompatibleComponentException;
import org.dealer.domain.model.CarModel;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfiguratorService implements ConfiguratorUseCase {

    private static final Set<ComponentType> REQUIRED_TYPES = Set.of(
            ComponentType.WHEELS,
            ComponentType.TRANSMISSION,
            ComponentType.STEERING,
            ComponentType.INTERIOR
    );

    private final CarModelRepository carModelRepository;

    @Override
    public Configuration buildConfiguration(UUID modelID, Map<ComponentType, ComponentOption> selectedOptions) {
        validateModelId(modelID);

        if (selectedOptions == null) {
            throw new DomainValidationException("Selected options must be not null");
        }

        CarModel model = getModelOrThrow(modelID);
        validateSelectedOptions(model, selectedOptions);

        return new Configuration(new EnumMap<>(selectedOptions));
    }

    @Override
    public int validate(UUID modelID, Configuration configuration) {
        validateConfigurationForModel(modelID, configuration);
        return calculateTotalPrice(modelID, configuration);
    }

    @Override
    public int calculateTotalPrice(UUID modelID, Configuration configuration) {
        CarModel model = validateConfigurationForModel(modelID, configuration);

        return model.getBasePrice()
                + configuration.getSelectedOptions().values().stream()
                .mapToInt(ComponentOption::getDiscount)
                .sum();
    }

    @Override
    public Map<ComponentType, List<ComponentOption>> getAvailableOptions(UUID modelID) {
        validateModelId(modelID);
        CarModel model = getModelOrThrow(modelID);
        return model.getAvailableOptions();
    }

    @Override
    public Map<ComponentType, Integer> priceBreakdown(UUID modelID, Configuration configuration) {
        validateConfigurationForModel(modelID, configuration);

        Map<ComponentType, Integer> result = new EnumMap<>(ComponentType.class);
        for (Map.Entry<ComponentType, ComponentOption> entry : configuration.getSelectedOptions().entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDiscount());
        }
        return result;
    }

    private CarModel validateConfigurationForModel(UUID modelID, Configuration configuration) {
        validateModelId(modelID);

        if (configuration == null) {
            throw new DomainValidationException("Configuration must be not null");
        }

        CarModel model = getModelOrThrow(modelID);
        validateSelectedOptions(model, configuration.getSelectedOptions());
        return model;
    }

    private CarModel getModelOrThrow(UUID modelID) {
        return carModelRepository.findByID(modelID)
                .orElseThrow(() -> new EntityNotFoundException("Car model not found: " + modelID));
    }

    private void validateModelId(UUID modelID) {
        if (modelID == null) {
            throw new DomainValidationException("ModelID must be not null");
        }
    }

    private void validateSelectedOptions(CarModel model, Map<ComponentType, ComponentOption> selectedOptions) {
        if (selectedOptions.isEmpty()) {
            throw new DomainValidationException("Configuration must contain selected options");
        }

        for (ComponentType requiredType : REQUIRED_TYPES) {
            if (!selectedOptions.containsKey(requiredType) || selectedOptions.get(requiredType) == null) {
                throw new DomainValidationException(
                        "Missing required component: " + readableComponentName(requiredType)
                );
            }
        }

        Map<ComponentType, List<ComponentOption>> availableOptions = model.getAvailableOptions();

        for (Map.Entry<ComponentType, ComponentOption> entry : selectedOptions.entrySet()) {
            ComponentType type = entry.getKey();
            ComponentOption selectedOption = entry.getValue();

            if (type == null) {
                throw new DomainValidationException("Component type must be not null");
            }

            if (selectedOption == null) {
                throw new DomainValidationException("Component option must be not null for type: " + type);
            }

            List<ComponentOption> allowedOptions = availableOptions.get(type);
            if (allowedOptions == null || allowedOptions.isEmpty()) {
                throw new DomainValidationException("No available options for component: " + type);
            }

            boolean optionExistsForModel = allowedOptions.stream()
                    .anyMatch(option -> option.getPartId().equals(selectedOption.getPartId()));

            if (!optionExistsForModel) {
                throw new IncompatibleComponentException(
                        "Selected " + readableComponentName(type)
                                + " is unavailable for model " + model.getBrand() + " " + model.getName()
                );
            }
        }
    }

    private String readableComponentName(ComponentType type) {
        return switch (type) {
            case WHEELS -> "wheels";
            case TRANSMISSION -> "transmission";
            case STEERING -> "steering";
            case INTERIOR -> "interior";
        };
    }
}
