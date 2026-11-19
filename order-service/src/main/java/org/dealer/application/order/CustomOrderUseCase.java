package org.dealer.application.order;

import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.enums.OrderStatusCustom;
import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomOrderUseCase {
    CustomOrder createOrder(UUID customerID, UUID modelID, Configuration configuration);
    CustomOrder getByID(UUID orderID);
    List<CustomOrder> findAll();
    List<CustomOrder> findByStatus(OrderStatusCustom status);
    CustomOrder changeStatus(UUID orderID, OrderStatusCustom newStatus);
    CustomOrder cancel(UUID orderID);
}
