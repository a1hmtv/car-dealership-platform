package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.AssemblyOrder;
import org.dealer.infrastructure.persistence.entity.AssemblyOrderEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssemblyOrderMapper {

    public AssemblyOrder toDomain(AssemblyOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AssemblyOrder(
                entity.getId(),
                entity.getSourceOrderId(),
                entity.getSourceOrderType(),
                entity.getCarId(),
                entity.getModelId(),
                entity.getRequiredPartIds(),
                entity.getWarehouseEmployeeId(),
                entity.getTraceId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isRemoved()
        );
    }

    public AssemblyOrderEntity toEntity(AssemblyOrder assemblyOrder) {
        if (assemblyOrder == null) {
            return null;
        }

        AssemblyOrderEntity entity = new AssemblyOrderEntity();
        entity.setId(assemblyOrder.getId());
        entity.setSourceOrderId(assemblyOrder.getSourceOrderId());
        entity.setSourceOrderType(assemblyOrder.getSourceOrderType());
        entity.setCarId(assemblyOrder.getCarId());
        entity.setModelId(assemblyOrder.getModelId());
        entity.setRequiredPartIds(List.copyOf(assemblyOrder.getRequiredPartIds()));
        entity.setWarehouseEmployeeId(assemblyOrder.getWarehouseEmployeeId());
        entity.setTraceId(assemblyOrder.getTraceId());
        entity.setStatus(assemblyOrder.getStatus());
        entity.setCreatedAt(assemblyOrder.getCreatedAt());
        entity.setUpdatedAt(assemblyOrder.getUpdatedAt());
        entity.setRemoved(assemblyOrder.isRemoved());
        return entity;
    }
}
