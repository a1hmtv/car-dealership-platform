package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.enums.OrderStatusCustom;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "custom_order")

public class CustomOrderEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private ManagerEntity manager;

    @ManyToOne
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModelEntity carModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusCustom status;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;
}
