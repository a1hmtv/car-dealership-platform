package org.dealer.application.configurator;

import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.configurator.Configuration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ConfiguratorUseCase {
    Configuration buildConfiguration(UUID modelID, Map<ComponentType, ComponentOption> selectedOptoins);
    int validate(UUID modelID, Configuration configuration);
    int calculateTotalPrice(UUID modelID, Configuration configuration);
    Map<ComponentType, List<ComponentOption>> getAvailableOptions(UUID modelID);
    Map<ComponentType, Integer> priceBreakdown(UUID modelID, Configuration configuration);
}
