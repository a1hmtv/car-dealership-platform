package org.dealer.application.order;

import org.dealer.application.car.CarAvailabilityClient;
import org.dealer.application.outbox.OutboxEventUseCase;
import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.application.ports.repository.InStockOrderRepository;
import org.dealer.application.ports.repository.ManagerRepository;
import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.Customer;
import org.dealer.domain.model.InStockOrder;
import org.dealer.domain.model.Manager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service("inStockOrderService")
public class InStockOrderService implements InStockOrderUseCase {

    private final InStockOrderRepository inStockOrderRepository;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;
    private final OutboxEventUseCase outboxEventUseCase;
    private final CarAvailabilityClient carAvailabilityClient;

    public InStockOrderService(
            InStockOrderRepository inStockOrderRepository,
            CustomerRepository customerRepository,
            ManagerRepository managerRepository,
            OutboxEventUseCase outboxEventUseCase,
            CarAvailabilityClient carAvailabilityClient
    ) {
        if (inStockOrderRepository == null) {
            throw new DomainValidationException("InStockOrderRepository must be not null");
        }
        if (customerRepository == null) {
            throw new DomainValidationException("CustomerRepository must be not null");
        }
        if (managerRepository == null) {
            throw new DomainValidationException("ManagerRepository must be not null");
        }
        if (outboxEventUseCase == null) {
            throw new DomainValidationException("OutboxEventUseCase must be not null");
        }

        if (carAvailabilityClient == null) {
            throw new DomainValidationException("CarAvailabilityClient must be not null");
        }
        this.carAvailabilityClient = carAvailabilityClient;

        this.inStockOrderRepository = inStockOrderRepository;
        this.customerRepository = customerRepository;
        this.managerRepository = managerRepository;
        this.outboxEventUseCase = outboxEventUseCase;
    }

    @Override
    @Transactional
    public InStockOrder createOrder(UUID customerID, UUID carID) {
        if (customerID == null) {
            throw new DomainValidationException("CustomerID must be not null");
        }
        if (carID == null) {
            throw new DomainValidationException("CarID must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")
                && !customerID.equals(getCurrentCustomerId())) {
            throw new AccessDeniedException("Access denied");
        }

        customerRepository.findByID(customerID)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerID));

        var car = carAvailabilityClient.getAvailableCarById(carID);
        if (car.price() == null || car.price().signum() <= 0) {
            throw new DomainValidationException("Car price must be positive");
        }

        Manager manager = pickAnyManager();
        UUID orderID = UUID.randomUUID();

        InStockOrder order = new InStockOrder(
                orderID,
                customerID,
                manager.getManagerID(),
                carID,
                OrderStatusInStock.CREATED,
                car.price()
        );

        inStockOrderRepository.save(order);
        return order;
    }

    @Override
    public InStockOrder getByID(UUID orderID) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER") && !isCurrentUserOwner(orderID)) {
            throw new AccessDeniedException("Access denied");
        }

        return inStockOrderRepository.findByID(orderID)
                .orElseThrow(() -> new EntityNotFoundException("InStockOrder not found: " + orderID));
    }

    @Override
    public List<InStockOrder> findAll() {
        if (hasRole("ADMIN") || hasRole("MANAGER")) {
            return inStockOrderRepository.findAll();
        }

        return inStockOrderRepository.findByCustomerId(getCurrentCustomerId());
    }

    @Override
    public List<InStockOrder> findByStatus(OrderStatusInStock status) {
        if (status == null) {
            throw new DomainValidationException("Status must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")) {
            throw new AccessDeniedException("Access denied");
        }

        return inStockOrderRepository.findAll()
                .stream()
                .filter(order -> order.getOrderStatus() == status)
                .toList();
    }

    @Override
    @Transactional
    public InStockOrder changeStatus(UUID orderID, OrderStatusInStock newStatus) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }
        if (newStatus == null) {
            throw new DomainValidationException("NewStatus must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER")) {
            throw new AccessDeniedException("Access denied");
        }

        InStockOrder order = inStockOrderRepository.findByIDForUpdate(orderID)
                .orElseThrow(() -> new EntityNotFoundException("InStockOrder not found: " + orderID));

        OrderStatusInStock current = order.getOrderStatus();
        if (!isAllowedTransition(current, newStatus)) {
            throw new DomainValidationException("Illegal status transition: " + current + " -> " + newStatus);
        }

        order.changeStatus(newStatus);
        inStockOrderRepository.save(order);

        if (newStatus == OrderStatusInStock.PAID) {
            createOrderSentForApprovalEvent(order);
        }

        return order;
    }

    @Override
    @Transactional
    public InStockOrder cancel(UUID orderID) {
        if (orderID == null) {
            throw new DomainValidationException("OrderID must be not null");
        }

        if (!hasRole("ADMIN") && !hasRole("MANAGER") && !isCurrentUserOwner(orderID)) {
            throw new AccessDeniedException("Access denied");
        }

        InStockOrder order = inStockOrderRepository.findByIDForUpdate(orderID)
                .orElseThrow(() -> new EntityNotFoundException("InStockOrder not found: " + orderID));

        OrderStatusInStock current = order.getOrderStatus();
        if (!isAllowedTransition(current, OrderStatusInStock.CANCELLED)) {
            throw new DomainValidationException(
                    "Illegal status transition: " + current + " -> " + OrderStatusInStock.CANCELLED
            );
        }

        order.changeStatus(OrderStatusInStock.CANCELLED);
        inStockOrderRepository.save(order);
        return order;
    }

    public boolean isCurrentUserOwner(UUID orderID) {
        UUID currentCustomerId = getCurrentCustomerId();

        return inStockOrderRepository.findByID(orderID)
                .map(order -> order.getCustomerID().equals(currentCustomerId))
                .orElse(false);
    }

    private void createOrderSentForApprovalEvent(InStockOrder order) {
        outboxEventUseCase.createEvent(
                order.getOrderID(),
                "IN_STOCK_ORDER",
                "ORDER_SENT_FOR_APPROVAL",
                Map.of(
                        "orderId", order.getOrderID(),
                        "orderType", "IN_STOCK",
                        "customerId", order.getCustomerID(),
                        "managerId", order.getManagerID(),
                        "carId", order.getCarID(),
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

    private boolean isAllowedTransition(OrderStatusInStock from, OrderStatusInStock to) {
        if (from == null || to == null || from == to) {
            return false;
        }

        return switch (from) {
            case CREATED -> to == OrderStatusInStock.ORDERED || to == OrderStatusInStock.CANCELLED;
            case ORDERED -> to == OrderStatusInStock.AWAITING_PAYMENT || to == OrderStatusInStock.CANCELLED;
            case AWAITING_PAYMENT -> to == OrderStatusInStock.PAID || to == OrderStatusInStock.CANCELLED;
            case READY_FOR_PICKUP -> to == OrderStatusInStock.COMPLETED;
            case PAID, COMPLETED, CANCELLED -> false;
        };
    }
}
