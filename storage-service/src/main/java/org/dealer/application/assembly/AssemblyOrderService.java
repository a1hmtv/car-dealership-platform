package org.dealer.application.assembly;

import org.dealer.application.outbox.StorageOutboxEventUseCase;
import org.dealer.application.ports.repository.AssemblyOrderRepository;
import org.dealer.application.ports.repository.CarRepository;
import org.dealer.application.ports.repository.CarModelRepository;
import org.dealer.application.ports.repository.PartStockRepository;
import org.dealer.domain.enums.CarStatus;
import org.dealer.domain.model.Car;
import org.dealer.domain.model.PartStock;
import org.dealer.domain.enums.AssemblyOrderStatus;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.AssemblyOrder;
import org.dealer.infrastructure.messaging.RabbitMqConfig;
import org.dealer.infrastructure.messaging.event.OrderResultEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class AssemblyOrderService implements AssemblyOrderUseCase {

    private final AssemblyOrderRepository assemblyOrderRepository;
    private final StorageOutboxEventUseCase outboxEventUseCase;
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final PartStockRepository partStockRepository;

    public AssemblyOrderService(
            AssemblyOrderRepository assemblyOrderRepository,
            StorageOutboxEventUseCase outboxEventUseCase,
            CarRepository carRepository,
            CarModelRepository carModelRepository,
            PartStockRepository partStockRepository
    ) {
        if (assemblyOrderRepository == null) {
            throw new DomainValidationException("AssemblyOrderRepository must be not null");
        }
        if (outboxEventUseCase == null) {
            throw new DomainValidationException("StorageOutboxEventUseCase must be not null");
        }

        this.assemblyOrderRepository = assemblyOrderRepository;
        this.outboxEventUseCase = outboxEventUseCase;
        this.carRepository = Objects.requireNonNull(carRepository);
        this.carModelRepository = Objects.requireNonNull(carModelRepository);
        this.partStockRepository = Objects.requireNonNull(partStockRepository);
    }

    @Override
    @Transactional
    public AssemblyOrder create(
            UUID sourceOrderId,
            String sourceOrderType,
            UUID carId,
            UUID modelId,
            List<UUID> requiredPartIds,
            UUID warehouseEmployeeId,
            String traceId
    ) {
        if (sourceOrderId == null) {
            throw new DomainValidationException("SourceOrderId must be not null");
        }
        if (sourceOrderType == null || sourceOrderType.isBlank()) {
            throw new DomainValidationException("SourceOrderType must be not blank");
        }
        if (!"IN_STOCK".equals(sourceOrderType) && !"CUSTOM".equals(sourceOrderType)) {
            throw new DomainValidationException("Unknown source order type: " + sourceOrderType);
        }
        if (assemblyOrderRepository.existsBySourceOrderId(sourceOrderId)) {
            throw new DomainValidationException("Assembly order already exists: " + sourceOrderId);
        }

        List<UUID> parts = requiredPartIds == null ? List.of() : List.copyOf(requiredPartIds);
        boolean reserved;
        if ("IN_STOCK".equals(sourceOrderType)) {
            reserved = carId != null && parts.isEmpty() && reserveCar(carId);
        } else {
            reserved = carId == null && modelId != null && !parts.isEmpty()
                    && carModelRepository.findByID(modelId).isPresent() && reserveParts(parts);
        }

        Instant now = Instant.now();

        AssemblyOrder assemblyOrder = new AssemblyOrder(
                UUID.randomUUID(),
                sourceOrderId,
                sourceOrderType,
                carId,
                modelId,
                parts,
                warehouseEmployeeId,
                traceId,
                reserved ? AssemblyOrderStatus.CREATED : AssemblyOrderStatus.FAIL,
                now,
                now,
                false
        );

        AssemblyOrder saved = assemblyOrderRepository.save(assemblyOrder);
        createOrderResultOutboxEventIfNeeded(saved);
        return saved;
    }

    @Override
    public AssemblyOrder getById(UUID id) {
        if (id == null) {
            throw new DomainValidationException("AssemblyOrder id must be not null");
        }

        return assemblyOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AssemblyOrder not found: " + id));
    }

    @Override
    public List<AssemblyOrder> findAll() {
        return assemblyOrderRepository.findActive();
    }

    @Override
    @Transactional
    public AssemblyOrder updateStatus(UUID id, AssemblyOrderStatus status) {
        if (status == null) {
            throw new DomainValidationException("AssemblyOrder status must be not null");
        }

        AssemblyOrder assemblyOrder = getForUpdate(id);
        if (assemblyOrder.isRemoved()) {
            throw new DomainValidationException("Removed assembly order cannot be changed");
        }
        if (assemblyOrder.getStatus() == status) {
            return assemblyOrder;
        }
        if (assemblyOrder.getStatus() != AssemblyOrderStatus.CREATED
                || status == AssemblyOrderStatus.CREATED) {
            throw new DomainValidationException("Illegal assembly status transition: "
                    + assemblyOrder.getStatus() + " -> " + status);
        }

        finishReservation(assemblyOrder, status == AssemblyOrderStatus.ASSEMBLED);
        assemblyOrder.changeStatus(status);

        AssemblyOrder saved = assemblyOrderRepository.save(assemblyOrder);
        createOrderResultOutboxEventIfNeeded(saved);

        return saved;
    }

    @Override
    @Transactional
    public AssemblyOrder assignWarehouseEmployee(UUID id, UUID warehouseEmployeeId) {
        if (warehouseEmployeeId == null) {
            throw new DomainValidationException("WarehouseEmployeeId must be not null");
        }

        AssemblyOrder assemblyOrder = getForUpdate(id);

        if (assemblyOrder.isRemoved()) {
            throw new DomainValidationException("Removed assembly order cannot be reassigned");
        }

        AssemblyOrder updated = new AssemblyOrder(
                assemblyOrder.getId(),
                assemblyOrder.getSourceOrderId(),
                assemblyOrder.getSourceOrderType(),
                assemblyOrder.getCarId(),
                assemblyOrder.getModelId(),
                assemblyOrder.getRequiredPartIds(),
                warehouseEmployeeId,
                assemblyOrder.getTraceId(),
                assemblyOrder.getStatus(),
                assemblyOrder.getCreatedAt(),
                Instant.now(),
                assemblyOrder.isRemoved()
        );

        return assemblyOrderRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        AssemblyOrder assemblyOrder = getForUpdate(id);
        if (assemblyOrder.isRemoved()) {
            return;
        }
        if (assemblyOrder.getStatus() == AssemblyOrderStatus.CREATED) {
            finishReservation(assemblyOrder, false);
            assemblyOrder.changeStatus(AssemblyOrderStatus.FAIL);
            createOrderResultOutboxEventIfNeeded(assemblyOrder);
        }
        assemblyOrder.markRemoved();
        assemblyOrderRepository.save(assemblyOrder);
    }

    private AssemblyOrder getForUpdate(UUID id) {
        if (id == null) {
            throw new DomainValidationException("AssemblyOrder id must be not null");
        }
        return assemblyOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("AssemblyOrder not found: " + id));
    }

    private boolean reserveCar(UUID carId) {
        Car car = carRepository.findByIDForUpdate(carId).orElse(null);
        if (car == null || car.getStatus() != CarStatus.IN_STOCK) {
            return false;
        }
        carRepository.save(withStatus(car, CarStatus.RESERVED));
        return true;
    }

    private Map<UUID, Integer> countParts(List<UUID> parts) {
        Map<UUID, Integer> quantities = new TreeMap<>();
        for (UUID part : parts) {
            quantities.merge(part, 1, Integer::sum);
        }
        return quantities;
    }

    private boolean reserveParts(List<UUID> parts) {
        Map<UUID, Integer> quantities = countParts(parts);
        Map<PartStock, Integer> reservations = new LinkedHashMap<>();
        // Lock and validate every row before modifying any of them.
        for (var item : quantities.entrySet()) {
            PartStock stock = partStockRepository.findByPartIdForUpdate(item.getKey()).orElse(null);
            if (stock == null || !stock.canReserve(item.getValue())) {
                return false;
            }
            reservations.put(stock, item.getValue());
        }
        reservations.forEach((stock, quantity) -> {
            stock.reserve(quantity);
            partStockRepository.save(stock);
        });
        return true;
    }

    private void finishReservation(AssemblyOrder order, boolean success) {
        if ("IN_STOCK".equals(order.getSourceOrderType())) {
            Car car = carRepository.findByIDForUpdate(order.getCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Reserved car not found"));
            if (car.getStatus() != CarStatus.RESERVED) {
                throw new DomainValidationException("Car is no longer reserved");
            }
            carRepository.save(withStatus(car, success ? CarStatus.SOLD : CarStatus.IN_STOCK));
        } else {
            for (var item : countParts(order.getRequiredPartIds()).entrySet()) {
                PartStock stock = partStockRepository.findByPartIdForUpdate(item.getKey())
                        .orElseThrow(() -> new EntityNotFoundException("Reserved part not found: " + item.getKey()));
                if (success) {
                    stock.consume(item.getValue());
                } else {
                    stock.release(item.getValue());
                }
                partStockRepository.save(stock);
            }
        }
    }

    private Car withStatus(Car car, CarStatus status) {
        return new Car(car.getId(), car.getCarModelId(), car.getVin(), car.getColor(), car.getPrice(),
                car.getBodyType(), car.getFuelType(), car.getTransmissionType(), car.getDriveType(),
                status, car.isAvailableForTestDrive());
    }

    private void createOrderResultOutboxEventIfNeeded(AssemblyOrder assemblyOrder) {
        if (assemblyOrder.getStatus() == AssemblyOrderStatus.ASSEMBLED) {
            createOrderResultOutboxEvent(
                    assemblyOrder,
                    "ORDER_APPROVED",
                    RabbitMqConfig.ORDER_APPROVED_ROUTING_KEY
            );
        }

        if (assemblyOrder.getStatus() == AssemblyOrderStatus.FAIL) {
            createOrderResultOutboxEvent(
                    assemblyOrder,
                    "ORDER_REJECTED",
                    RabbitMqConfig.ORDER_REJECTED_ROUTING_KEY
            );
        }
    }

    private void createOrderResultOutboxEvent(
            AssemblyOrder assemblyOrder,
            String eventType,
            String routingKey
    ) {
        OrderResultEvent event = new OrderResultEvent(
                assemblyOrder.getSourceOrderId(),
                assemblyOrder.getSourceOrderType(),
                assemblyOrder.getId(),
                eventType,
                assemblyOrder.getTraceId()
        );

        outboxEventUseCase.createEvent(
                assemblyOrder.getId(),
                "ASSEMBLY_ORDER",
                eventType,
                RabbitMqConfig.ORDER_EXCHANGE,
                routingKey,
                event
        );
    }
}
