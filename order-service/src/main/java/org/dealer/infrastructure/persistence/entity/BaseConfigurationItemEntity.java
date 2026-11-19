package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.configurator.ComponentType;

@Getter
@Setter
@Entity
@Table(name = "base_configuration_item")

public class BaseConfigurationItemEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartEntity part;

    @ManyToOne
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModelEntity carModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    private ComponentType componentType;

}
