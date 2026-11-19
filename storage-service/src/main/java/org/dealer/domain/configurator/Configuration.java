package org.dealer.domain.configurator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Configuration {

    private static final Set<ComponentType> REQUIRED_TYPES = Set.of(
            ComponentType.WHEELS,
            ComponentType.TRANSMISSION,
            ComponentType.STEERING,
            ComponentType.INTERIOR
    );

    private final Map<ComponentType, ComponentOption> selectedOptions;

    public Configuration() {
        this.selectedOptions = new EnumMap<>(ComponentType.class);
    }

    public Configuration(Map<ComponentType, ComponentOption> selectedOptions) {
        Objects.requireNonNull(selectedOptions, "selectedOptions must be not null");
        this.selectedOptions = new EnumMap<>(ComponentType.class);
        this.selectedOptions.putAll(selectedOptions);
    }

    public static Configuration empty() {
        return new Configuration();
    }

    public void selectOption(ComponentOption option) {
        Objects.requireNonNull(option, "option must be not null");
        Objects.requireNonNull(option.getType(), "option.type must be not null");
        selectedOptions.put(option.getType(), option);
    }

    public ComponentOption get(ComponentType type) {
        Objects.requireNonNull(type, "type must be not null");
        return selectedOptions.get(type);
    }

    public boolean isValid() {
        return selectedOptions.keySet().containsAll(REQUIRED_TYPES);
    }

    public Map<ComponentType, ComponentOption> getSelectedOptions() {
        return Collections.unmodifiableMap(selectedOptions);
    }
}