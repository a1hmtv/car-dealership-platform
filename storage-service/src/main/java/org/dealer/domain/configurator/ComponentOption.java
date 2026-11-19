package org.dealer.domain.configurator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ComponentOption {
    private final UUID partId;
    private final String name;
    private final ComponentType type;
    private final int discount;
    private final Set<String> compatibleModels;

    public boolean isCompatibleWith(String modelID) {
        if (modelID == null) {
            return false;
        }
        return compatibleModels.contains(modelID);
    }
}
