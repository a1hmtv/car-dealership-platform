package org.dealer.application.order;

import org.dealer.application.configurator.ConfiguratorUseCase;
import org.dealer.application.outbox.OutboxEventUseCase;
import org.dealer.application.ports.repository.CarModelRepository;
import org.dealer.application.ports.repository.CustomOrderRepository;
import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.application.ports.repository.ManagerRepository;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.enums.OrderStatusCustom;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.CarModel;
import org.dealer.domain.model.CustomOrder;
import org.dealer.domain.model.Customer;
import org.dealer.domain.model.Manager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service("customOrderService")
public class CustomOrderService implements CustomOrderUseCase {

    private final CustomOrderRepository customOrderRepository;
    private final CustomerRepository customerRepository;
    private final CarModelRepository carModelRepository;
    private final ManagerRepository managerRepository;
    private final ConfiguratorUseCase configuratorUseCase;
    private final OutboxEventUseCase outboxEventUseCase;

    public CustomOrderService(
            CustomOrderRepository customOrderRepository,
            CustomerRepository customerRepository,
            CarModelRepository carModelRepository,
            ManagerRepository managerRepository,
            ConfiguratorUseCase configuratorUseCase,
            OutboxEventUseCase outboxEventUseCase
    ) {
        if (customOrderRepository == null) {
            throw new DomainValidationException("CustomOrderRepository must be not null");
        }
        if (customerRepository == null) {
            throw new DomainValidationException("CustomerRepository must be not null");
        }
        if (carModelRepository == null) {
            throw new DomainValidationException("CarModelRepository must be not null");
        }
        if (managerRepository == null) {
            throw new DomainValidationException("ManagerRepository must be not null");
        }
        if (configuratorUseCase == null) {
            throw new DomainValidationException("ConfiguratorUseCase must be not null");
        }
        if (outboxEventUseCase == null) {
            throw new DomainValidationException("OutboxEventUseCase must be not null");
        }

        this.customOrderRepository = customOrderRepository;
        this.customerRepository = customerRepository;
        this.carModelRepository = carModelRepository;
        this.managerRepository = managerRepository;
        this.configuratorUseCase = configuratorUseCase;
        this.outboxEventUseCase = outboxEventUseCase;
    }

    @Override
    @Transactional
    public CustomOrder createOrder(UUID customerID, UUID modelID, Configuration configuration) {
        if (customerID == null) {
            throw new DomainValidationException("CustomerID must be not null");
        }
        if (modelID == null) {
            throw new DomainValidationException("ModelID must be not null");
        }
        if (configuration == null) {
            throw new DomainValidationException("Configuration must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")
                && !customerID.equals(getCurrentCustomerId())) {
            throw new AccessDeniedException("Access denied");
        }

        customerRepository.findByID(customerID)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerID));

        CarModel model = carModelRepository.findByID(modelID)
                .orElseThrow(() -> new EntityNotFoundException("CarModel not found: " + modelID));

        Manager manager = pickAnyManager();

        configuratorUseCase.validate(modelID, configuration);
        int totalPrice = configuratorUseCase.calculateTotalPrice(modelID, configuration);

        UUID orderID = UUID.randomUUID();

        CustomOrder order = new CustomOrder(
                orderID,
                customerID,
                manager.getManagerID(),
                model.getId(),
                configuration,
                OrderStatusCustom.CREATED,
                BigDecimal.valueOf(totalPrice)
        );

        customOrderRepository.save(order);
        return order;
    }

    @Override
    public CustomOrder getByID(UUID orderID) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER") && !isCurrentUserOwner(orderID)) {
            throw new AccessDeniedException("Access denied");
        }

        return customOrderRepository.findByID(orderID)
                .orElseThrow(() -> new EntityNotFoundException("CustomOrder not found: " + orderID));
    }

    @Override
    public List<CustomOrder> findAll() {
        if (hasRole("ADMIN") || hasRole("MANAGER")) {
            return customOrderRepository.findAll();
        }

        return customOrderRepository.findByCustomerId(getCurrentCustomerId());
    }

    @Override
    public List<CustomOrder> findByStatus(OrderStatusCustom status) {
        if (status == null) {
            throw new DomainValidationException("Status must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")) {
            throw new AccessDeniedException("Access denied");
        }

        return customOrderRepository.findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == status)
                .toList();
    }

    @Override
    @Transactional
    public CustomOrder changeStatus(UUID orderID, OrderStatusCustom newStatus) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }
        if (newStatus == null) {
            throw new DomainValidationException("NewStatus must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")) {
            throw new AccessDeniedException("Access denied");
        }

        CustomOrder order = customOrderRepository.findByIDForUpdate(orderID)
                .orElseThrow(() -> new EntityNotFoundException("CustomOrder not found: " + orderID));

        OrderStatusCustom current = order.getOrderStatus();
        if (!isAllowedTransition(current, newStatus)) {
            throw new DomainValidationException("Illegal status transition: " + current + " -> " + newStatus);
        }

        order.changeStatus(newStatus);
        customOrderRepository.save(order);

        if (newStatus == OrderStatusCustom.PAID) {
            createOrderSentForApprovalEvent(order);
        }

        return order;
    }

    @Override
    @Transactional
    public CustomOrder cancel(UUID orderID) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER") && !isCurrentUserOwner(orderID)) {
            throw new AccessDeniedException("Access denied");
        }

        CustomOrder order = customOrderRepository.findByIDForUpdate(orderID)
                .orElseThrow(() -> new EntityNotFoundException("CustomOrder not found: " + orderID));

        OrderStatusCustom current = order.getOrderStatus();
        if (!isAllowedTransition(current, OrderStatusCustom.CANCELLED)) {
            throw new DomainValidationException(
                    "Illegal status transition: " + current + " -> " + OrderStatusCustom.CANCELLED
            );
        }

        order.changeStatus(OrderStatusCustom.CANCELLED);
        customOrderRepository.save(order);
        return order;
    }

    public boolean isCurrentUserOwner(UUID orderID) {
        UUID currentCustomerId = getCurrentCustomerId();

        return customOrderRepository.findByID(orderID)
                .map(order -> order.getCustomerID().equals(currentCustomerId))
                .orElse(false);
    }

    private void createOrderSentForApprovalEvent(CustomOrder order) {
        List<UUID> requiredPartIds = order.getConfiguration()
                .getSelectedOptions()
                .values()
                .stream()
                .map(option -> option.getPartId())
                .toList();

        if (requiredPartIds.isEmpty()) {
            throw new DomainValidationException("Custom order configuration must contain required parts");
        }

        outboxEventUseCase.createEvent(
                order.getOrderID(),
                "CUSTOM_ORDER",
                "ORDER_SENT_FOR_APPROVAL",
                Map.of(
                        "orderId", order.getOrderID(),
                        "orderType", "CUSTOM",
                        "customerId", order.getCustomerID(),
                        "managerId", order.getManagerID(),
                        "modelId", order.getModelID(),
                        "requiredPartIds", requiredPartIds,
                        "status", order.getOrderStatus().name(),
                        "totalPrice", order.getTotalPrice(),
                        "traceId", UUID.randomUUID().toString()
                )
        );
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

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authenticated user email is missing");
        }
        return email;
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    private Manager pickAnyManager() {
        return managerRepository.findAll()
                .stream()
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("No managers available"));
    }

    private boolean isAllowedTransition(OrderStatusCustom from, OrderStatusCustom to) {
        if (from == null || to == null || from == to) {
            return false;
        }

        return switch (from) {
            case CREATED -> to == OrderStatusCustom.ORDERED || to == OrderStatusCustom.CANCELLED;
            case ORDERED -> to == OrderStatusCustom.AWAITING_PAYMENT || to == OrderStatusCustom.CANCELLED;
            case AWAITING_PAYMENT -> to == OrderStatusCustom.PAID || to == OrderStatusCustom.CANCELLED;
            case READY_FOR_PICKUP -> to == OrderStatusCustom.COMPLETED;
            case PAID -> to == OrderStatusCustom.AWAITING_DELIVERY;
            case AWAITING_DELIVERY, COMPLETED, CANCELLED -> false;
        };
    }
}
