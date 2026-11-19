package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "part_compatible_car_model")

public class PartCompatibleCarModelEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "part_id", nullable = false)
    private PartEntity part;

    @ManyToOne
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModelEntity carModel;
}
