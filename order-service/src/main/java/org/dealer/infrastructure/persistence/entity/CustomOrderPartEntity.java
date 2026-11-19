package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "custom_order_part")

public class CustomOrderPartEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "custom_order_id", nullable = false)
    private CustomOrderEntity customOrder;

    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartEntity part;
}
