package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.model.CustomOrder;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.dealer.infrastructure.persistence.entity.CustomOrderEntity;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomOrderMapper {

    public CustomOrder toDomain(CustomOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CustomOrder(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getManager().getId(),
                entity.getCarModel().getId(),
                Configuration.empty(),
                entity.getStatus(),
                entity.getFinalPrice()
        );
    }

    public CustomOrderEntity toEntity(
            CustomOrder order,
            CustomerEntity customer,
            ManagerEntity manager,
            CarModelEntity carModel
    ) {
        if (order == null) {
            return null;
        }

        if (customer == null || manager == null || carModel == null) {
            throw new IllegalArgumentException("customer, manager and carModel must be not null");
        }

        CustomOrderEntity entity = new CustomOrderEntity();
        entity.setId(order.getOrderID());
        entity.setCustomer(customer);
        entity.setManager(manager);
        entity.setCarModel(carModel);
        entity.setStatus(order.getOrderStatus());
        entity.setFinalPrice(order.getTotalPrice());
        return entity;
    }
}
