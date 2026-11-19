package org.dealer.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.dealer.domain.enums.AssemblyOrderStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "assembly_order")
public class AssemblyOrderEntity extends BaseEntity {

    @Column(name = "source_order_id", nullable = false)
    private UUID sourceOrderId;

    @Column(name = "source_order_type", nullable = false)
    private String sourceOrderType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "model_id")
    private UUID modelId;

    @ElementCollection
    @CollectionTable(
            name = "assembly_order_required_part",
            joinColumns = @JoinColumn(name = "assembly_order_id")
    )
    @Column(name = "part_id")
    private List<UUID> requiredPartIds = new ArrayList<>();

    @Column(name = "warehouse_employee_id")
    private UUID warehouseEmployeeId;

    @Column(name = "trace_id")
    private String traceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssemblyOrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "removed", nullable = false)
    private boolean removed;
}
