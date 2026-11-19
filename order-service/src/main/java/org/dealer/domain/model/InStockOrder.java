package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class InStockOrder {

    private final UUID orderID;
    private final UUID customerID;
    private final UUID managerID;
    private final UUID carID;

    private OrderStatusInStock orderStatus;
    private BigDecimal totalPrice;

    public InStockOrder(
            UUID orderID,
            UUID customerID,
            UUID managerID,
            UUID carID,
            OrderStatusInStock orderStatus,
            BigDecimal totalPrice
    ) {
        if (orderID == null) throw new DomainValidationException("OrderID must be not null");
        if (customerID == null) throw new DomainValidationException("CustomerID must be not null");
        if (managerID == null) throw new DomainValidationException("ManagerID must be not null");
        if (carID == null) throw new DomainValidationException("CarID must be not null");
        if (orderStatus == null) throw new DomainValidationException("OrderStatus must be not null");
        if (totalPrice == null) throw new DomainValidationException("TotalPrice must be >= 0");

        this.orderID = orderID;
        this.customerID = customerID;
        this.managerID = managerID;
        this.carID = carID;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
    }

    public void changeStatus(OrderStatusInStock newStatus) {
        if (newStatus == null) throw new DomainValidationException("NewStatus must be not null");
        this.orderStatus = newStatus;
    }
}