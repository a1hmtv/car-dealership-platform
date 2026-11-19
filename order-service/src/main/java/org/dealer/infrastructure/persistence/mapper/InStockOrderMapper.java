package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.InStockOrder;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.InStockOrderEntity;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.springframework.stereotype.Component;

@Component
public class InStockOrderMapper {

    public InStockOrder toDomain(InStockOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InStockOrder(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getManager().getId(),
                entity.getCarId(),
                entity.getStatus(),
                entity.getTotalPrice()
        );
    }

    public InStockOrderEntity toEntity(
            InStockOrder order,
            CustomerEntity customer,
            ManagerEntity manager
    ) {
        if (order == null) {
            return null;
        }

        if (customer == null || manager == null) {
            throw new IllegalArgumentException("customer and manager must be not null");
        }

        InStockOrderEntity entity = new InStockOrderEntity();
        entity.setId(order.getOrderID());
        entity.setCustomer(customer);
        entity.setManager(manager);
        entity.setCarId(order.getCarID());
        entity.setStatus(order.getOrderStatus());
        entity.setTotalPrice(order.getTotalPrice());
        return entity;
    }
}
