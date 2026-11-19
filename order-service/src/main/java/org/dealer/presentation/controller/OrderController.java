package org.dealer.presentation.controller;

import org.dealer.application.configurator.ConfiguratorUseCase;
import org.dealer.application.order.CustomOrderUseCase;
import org.dealer.application.order.InStockOrderUseCase;
import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.ComponentType;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.enums.OrderStatusCustom;
import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.CustomOrder;
import org.dealer.domain.model.Customer;
import org.dealer.domain.model.InStockOrder;
import org.dealer.presentation.dto.order.ChangeOrderStatusRequestDto;
import org.dealer.presentation.dto.order.CreateCustomOrderRequestDto;
import org.dealer.presentation.dto.order.CreateInStockOrderRequestDto;
import org.dealer.presentation.dto.order.CustomOrderResponseDto;
import org.dealer.presentation.dto.order.InStockOrderResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final InStockOrderUseCase inStockOrderUseCase;
    private final CustomOrderUseCase customOrderUseCase;
    private final ConfiguratorUseCase configuratorUseCase;
    private final CustomerRepository customerRepository;

    public OrderController(
            InStockOrderUseCase inStockOrderUseCase,
            CustomOrderUseCase customOrderUseCase,
            ConfiguratorUseCase configuratorUseCase,
            CustomerRepository customerRepository
    ) {
        this.inStockOrderUseCase = inStockOrderUseCase;
        this.customOrderUseCase = customOrderUseCase;
        this.configuratorUseCase = configuratorUseCase;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockOrderResponseDto createInStockOrder(@RequestBody CreateInStockOrderRequestDto request) {
        UUID currentCustomerId = getCurrentCustomerId();

        InStockOrder order = inStockOrderUseCase.createOrder(
                currentCustomerId,
                request.getCarId()
        );
        return toInStockDto(order);
    }

    @GetMapping("/in-stock/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public InStockOrderResponseDto getInStockOrder(@PathVariable UUID id) {
        return toInStockDto(inStockOrderUseCase.getByID(id));
    }

    @GetMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<InStockOrderResponseDto> getAllInStockOrders() {
        return inStockOrderUseCase.findAll().stream()
                .map(this::toInStockDto)
                .toList();
    }

    @PatchMapping("/in-stock/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockOrderResponseDto changeInStockOrderStatus(
            @PathVariable UUID id,
            @RequestBody ChangeOrderStatusRequestDto request
    ) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new DomainValidationException("Status must be not blank");
        }

        OrderStatusInStock status = OrderStatusInStock.valueOf(request.getStatus().toUpperCase());
        return toInStockDto(inStockOrderUseCase.changeStatus(id, status));
    }

    @PostMapping("/custom")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomOrderResponseDto createCustomOrder(@RequestBody CreateCustomOrderRequestDto request) {
        UUID currentCustomerId = getCurrentCustomerId();
        Configuration configuration = toConfiguration(request);

        CustomOrder order = customOrderUseCase.createOrder(
                currentCustomerId,
                request.getModelId(),
                configuration
        );

        return toCustomDto(order);
    }

    @GetMapping("/custom/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public CustomOrderResponseDto getCustomOrder(@PathVariable UUID id) {
        return toCustomDto(customOrderUseCase.getByID(id));
    }

    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CustomOrderResponseDto> getAllCustomOrders() {
        return customOrderUseCase.findAll().stream()
                .map(this::toCustomDto)
                .toList();
    }

    @PatchMapping("/custom/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomOrderResponseDto changeCustomOrderStatus(
            @PathVariable UUID id,
            @RequestBody ChangeOrderStatusRequestDto request
    ) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new DomainValidationException("Status must be not blank");
        }

        OrderStatusCustom status = OrderStatusCustom.valueOf(request.getStatus().toUpperCase());
        return toCustomDto(customOrderUseCase.changeStatus(id, status));
    }

    private UUID getCurrentCustomerId() {
        String email = getCurrentUserEmail();

        Customer customer = customerRepository.findAll().stream()
                .filter(item -> item.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found for authenticated user email: " + email
                ));

        return customer.getCustomerID();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authenticated JWT user not found in SecurityContext");
        }

        return jwt.getClaimAsString("email");
    }

    private InStockOrderResponseDto toInStockDto(InStockOrder order) {
        InStockOrderResponseDto dto = new InStockOrderResponseDto();
        dto.setOrderId(order.getOrderID());
        dto.setCustomerId(order.getCustomerID());
        dto.setManagerId(order.getManagerID());
        dto.setCarId(order.getCarID());
        dto.setStatus(order.getOrderStatus().name());
        dto.setTotalPrice(order.getTotalPrice());
        return dto;
    }

    private CustomOrderResponseDto toCustomDto(CustomOrder order) {
        CustomOrderResponseDto dto = new CustomOrderResponseDto();
        dto.setOrderId(order.getOrderID());
        dto.setCustomerId(order.getCustomerID());
        dto.setManagerId(order.getManagerID());
        dto.setModelId(order.getModelID());
        dto.setStatus(order.getOrderStatus().name());
        dto.setTotalPrice(order.getTotalPrice());

        Map<String, UUID> selectedOptions = order.getConfiguration()
                .getSelectedOptions()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().getPartId()
                ));

        dto.setSelectedOptions(selectedOptions);
        return dto;
    }

    private Configuration toConfiguration(CreateCustomOrderRequestDto request) {
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
}
