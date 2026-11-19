package org.dealer.presentation.controller;

import org.dealer.application.assembly.AssemblyOrderUseCase;
import org.dealer.domain.enums.AssemblyOrderStatus;
import org.dealer.domain.model.AssemblyOrder;
import org.dealer.presentation.dto.assembly.AssemblyOrderResponseDto;
import org.dealer.presentation.dto.assembly.CreateAssemblyOrderRequestDto;
import org.dealer.presentation.dto.assembly.UpdateAssemblyOrderRequestDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assembly-orders")
@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_ADMIN')")
public class AssemblyOrderController {

    private final AssemblyOrderUseCase assemblyOrderUseCase;

    public AssemblyOrderController(AssemblyOrderUseCase assemblyOrderUseCase) {
        this.assemblyOrderUseCase = assemblyOrderUseCase;
    }

    @PostMapping
    public AssemblyOrderResponseDto create(@RequestBody CreateAssemblyOrderRequestDto request) {
        AssemblyOrder assemblyOrder = assemblyOrderUseCase.create(
                request.getSourceOrderId(),
                request.getSourceOrderType(),
                request.getCarId(),
                request.getModelId(),
                request.getRequiredPartIds(),
                request.getWarehouseEmployeeId(),
                null
        );

        return toDto(assemblyOrder);
    }

    @GetMapping
    public List<AssemblyOrderResponseDto> findAll() {
        return assemblyOrderUseCase.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public AssemblyOrderResponseDto getById(@PathVariable UUID id) {
        return toDto(assemblyOrderUseCase.getById(id));
    }

    @PutMapping("/{id}")
    public AssemblyOrderResponseDto update(
            @PathVariable UUID id,
            @RequestBody UpdateAssemblyOrderRequestDto request
    ) {
        AssemblyOrder assemblyOrder = assemblyOrderUseCase.getById(id);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            assemblyOrder = assemblyOrderUseCase.updateStatus(
                    id,
                    AssemblyOrderStatus.valueOf(request.getStatus().toUpperCase())
            );
        }

        if (request.getWarehouseEmployeeId() != null) {
            assemblyOrder = assemblyOrderUseCase.assignWarehouseEmployee(
                    id,
                    request.getWarehouseEmployeeId()
            );
        }

        return toDto(assemblyOrder);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        assemblyOrderUseCase.delete(id);
    }

    private AssemblyOrderResponseDto toDto(AssemblyOrder assemblyOrder) {
        AssemblyOrderResponseDto dto = new AssemblyOrderResponseDto();
        dto.setId(assemblyOrder.getId());
        dto.setSourceOrderId(assemblyOrder.getSourceOrderId());
        dto.setSourceOrderType(assemblyOrder.getSourceOrderType());
        dto.setCarId(assemblyOrder.getCarId());
        dto.setModelId(assemblyOrder.getModelId());
        dto.setRequiredPartIds(assemblyOrder.getRequiredPartIds());
        dto.setWarehouseEmployeeId(assemblyOrder.getWarehouseEmployeeId());
        dto.setTraceId(assemblyOrder.getTraceId());
        dto.setStatus(assemblyOrder.getStatus().name());
        dto.setCreatedAt(assemblyOrder.getCreatedAt());
        dto.setUpdatedAt(assemblyOrder.getUpdatedAt());
        dto.setRemoved(assemblyOrder.isRemoved());
        return dto;
    }
}
