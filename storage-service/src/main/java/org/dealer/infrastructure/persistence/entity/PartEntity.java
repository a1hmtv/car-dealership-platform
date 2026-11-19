package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.configurator.ComponentType;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "part")

public class PartEntity extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

    @Column(name = "price_delta", nullable = false)
    private BigDecimal priceDelta;
}
