package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.CustomOrderRepository;
import org.dealer.domain.configurator.ComponentOption;
import org.dealer.domain.configurator.Configuration;
import org.dealer.domain.model.CustomOrder;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.dealer.infrastructure.persistence.entity.CustomOrderEntity;
import org.dealer.infrastructure.persistence.entity.CustomOrderPartEntity;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.dealer.infrastructure.persistence.entity.PartEntity;
import org.dealer.infrastructure.persistence.jpa.CarModelJpaRepository;
import org.dealer.infrastructure.persistence.jpa.CustomOrderJpaRepository;
import org.dealer.infrastructure.persistence.jpa.CustomOrderPartJpaRepository;
import org.dealer.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.dealer.infrastructure.persistence.jpa.ManagerJpaRepository;
import org.dealer.infrastructure.persistence.jpa.PartJpaRepository;
import org.dealer.infrastructure.persistence.mapper.CustomOrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class JpaCustomOrderRepositoryAdapter implements CustomOrderRepository {

    private final CustomOrderJpaRepository customOrderJpaRepository;
    private final CustomOrderPartJpaRepository customOrderPartJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;
    private final ManagerJpaRepository managerJpaRepository;
    private final CarModelJpaRepository carModelJpaRepository;
    private final PartJpaRepository partJpaRepository;
    private final CustomOrderMapper customOrderMapper;

    public JpaCustomOrderRepositoryAdapter(
            CustomOrderJpaRepository customOrderJpaRepository,
            CustomOrderPartJpaRepository customOrderPartJpaRepository,
            CustomerJpaRepository customerJpaRepository,
            ManagerJpaRepository managerJpaRepository,
            CarModelJpaRepository carModelJpaRepository,
            PartJpaRepository partJpaRepository,
            CustomOrderMapper customOrderMapper
    ) {
        this.customOrderJpaRepository = customOrderJpaRepository;
        this.customOrderPartJpaRepository = customOrderPartJpaRepository;
        this.customerJpaRepository = customerJpaRepository;
        this.managerJpaRepository = managerJpaRepository;
        this.carModelJpaRepository = carModelJpaRepository;
        this.partJpaRepository = partJpaRepository;
        this.customOrderMapper = customOrderMapper;
    }

    @Override
    public Optional<CustomOrder> findByIDForUpdate(UUID id) {
        return customOrderJpaRepository.findForUpdate(id)
                .map(this::toDomainWithConfiguration);
    }

    @Override
    public Optional<CustomOrder> findByID(UUID id) {
        return customOrderJpaRepository.findById(id)
                .map(this::toDomainWithConfiguration);
    }

    @Override
    public List<CustomOrder> findAll() {
        return customOrderJpaRepository.findAll().stream()
                .map(this::toDomainWithConfiguration)
                .toList();
    }

    @Override
    public List<CustomOrder> findByCustomerId(UUID customerId) {
        return customOrderJpaRepository.findByCustomer_Id(customerId).stream()
                .map(this::toDomainWithConfiguration)
                .toList();
    }

    @Override
    public CustomOrder save(CustomOrder customOrder) {
        CustomerEntity customer = customerJpaRepository.findById(customOrder.getCustomerID())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customOrder.getCustomerID()));

        ManagerEntity manager = managerJpaRepository.findById(customOrder.getManagerID())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + customOrder.getManagerID()));

        CarModelEntity carModel = carModelJpaRepository.findById(customOrder.getModelID())
                .orElseThrow(() -> new IllegalArgumentException("Car model not found: " + customOrder.getModelID()));

        CustomOrderEntity entity = customOrderMapper.toEntity(customOrder, customer, manager, carModel);
        CustomOrderEntity savedEntity = customOrderJpaRepository.save(entity);

        customOrderPartJpaRepository.deleteByCustomOrder_Id(savedEntity.getId());

        for (ComponentOption option : customOrder.getConfiguration().getSelectedOptions().values()) {
            PartEntity part = partJpaRepository.findById(option.getPartId())
                    .orElseThrow(() -> new IllegalArgumentException("Part not found: " + option.getPartId()));

            CustomOrderPartEntity link = new CustomOrderPartEntity();
            link.setId(UUID.randomUUID());
            link.setCustomOrder(savedEntity);
            link.setPart(part);

            customOrderPartJpaRepository.save(link);
        }

        return toDomainWithConfiguration(savedEntity);
    }

    @Override
    public void deleteByID(UUID id) {
        customOrderPartJpaRepository.deleteByCustomOrder_Id(id);
        customOrderJpaRepository.deleteById(id);
    }

    private CustomOrder toDomainWithConfiguration(CustomOrderEntity entity) {
        Configuration configuration = new Configuration();

        List<CustomOrderPartEntity> parts = customOrderPartJpaRepository.findByCustomOrder_Id(entity.getId());

        for (CustomOrderPartEntity link : parts) {
            PartEntity part = link.getPart();

            ComponentOption option = new ComponentOption(
                    part.getId(),
                    part.getName(),
                    part.getComponentType(),
                    part.getPriceDelta().intValue(),
                    Set.of()
            );

            configuration.selectOption(option);
        }

        return new CustomOrder(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getManager().getId(),
                entity.getCarModel().getId(),
                configuration,
                entity.getStatus(),
                entity.getFinalPrice()
        );
    }
}
