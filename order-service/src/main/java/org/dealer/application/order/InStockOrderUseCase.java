package org.dealer.application.order;

import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.model.InStockOrder;

import java.util.List;
import java.util.UUID;

public interface InStockOrderUseCase {
    InStockOrder createOrder(UUID customerID, UUID carID);
    InStockOrder getByID(UUID orderID);
    List<InStockOrder> findAll();
    List<InStockOrder> findByStatus(OrderStatusInStock status);
    InStockOrder changeStatus(UUID orderID, OrderStatusInStock newStatus);
    InStockOrder cancel(UUID orderID);

}
