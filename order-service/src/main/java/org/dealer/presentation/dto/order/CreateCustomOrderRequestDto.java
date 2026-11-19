package org.dealer.presentation.dto.order;

import java.util.Map;
import java.util.UUID;

public class CreateCustomOrderRequestDto {
    private UUID modelId;
    private Map<String, UUID> selectedOptions;

    public UUID getModelId() {
        return modelId;
    }

    public void setModelId(UUID modelId) {
        this.modelId = modelId;
    }

    public Map<String, UUID> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(Map<String, UUID> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }
}
