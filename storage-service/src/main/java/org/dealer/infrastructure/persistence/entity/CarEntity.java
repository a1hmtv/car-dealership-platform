package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.enums.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "car")

public class CarEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModelEntity carModel;

    @Column(name = "vin", nullable = false)
    private String vin;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", nullable = false)
    private TransmissionType transmissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "drive_type", nullable = false)
    private DriveType driveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;

    @Column(name = "available_for_test_drive", nullable = false)
    private boolean availableForTestDrive;
}
