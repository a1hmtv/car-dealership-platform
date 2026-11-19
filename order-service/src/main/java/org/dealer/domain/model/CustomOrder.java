package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.enums.OrderStatusCustom;
import org.dealer.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class CustomOrder {

    private final UUID orderID;
    private final UUID customerID;
    private final UUID managerID;
    private final UUID modelID;
    private final Configuration configuration;

    private OrderStatusCustom orderStatus;
    private BigDecimal totalPrice;

    public CustomOrder(
            UUID orderID,
            UUID customerID,
            UUID managerID,
            UUID modelID,
            Configuration configuration,
            OrderStatusCustom orderStatus,
            BigDecimal totalPrice
    ) {
        if (orderID == null) throw new DomainValidationException("OrderID must be not null");
        if (customerID == null) throw new DomainValidationException("CustomerID must be not null");
        if (managerID == null) throw new DomainValidationException("ManagerID must be not null");
        if (modelID == null) throw new DomainValidationException("ModelID must be not null");
        if (configuration == null) throw new DomainValidationException("Configuration must be not null");
        if (orderStatus == null) throw new DomainValidationException("OrderStatus must be not null");
        if (totalPrice == null) throw new DomainValidationException("TotalPrice must be not null");
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("TotalPrice must be >= 0");
        }

        this.orderID = orderID;
        this.customerID = customerID;
        this.managerID = managerID;
        this.modelID = modelID;
        this.configuration = configuration;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
    }

    public CustomOrder(
            UUID orderID,
            UUID customerID,
            UUID managerID,
            UUID modelID,
            OrderStatusCustom orderStatus,
            int totalPrice
    ) {
        this(
                orderID,
                customerID,
                managerID,
                modelID,
                Configuration.empty(),
                orderStatus,
                BigDecimal.valueOf(totalPrice)
        );
    }

    public void changeStatus(OrderStatusCustom newStatus) {
        if (newStatus == null) throw new DomainValidationException("NewStatus must be not null");
        this.orderStatus = newStatus;
    }
}
