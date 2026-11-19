package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.InStockOrderRepository;
import org.dealer.domain.model.InStockOrder;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.InStockOrderEntity;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.dealer.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.dealer.infrastructure.persistence.jpa.InStockOrderJpaRepository;
import org.dealer.infrastructure.persistence.jpa.ManagerJpaRepository;
import org.dealer.infrastructure.persistence.mapper.InStockOrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaInStockOrderRepositoryAdapter implements InStockOrderRepository {

    private final InStockOrderJpaRepository inStockOrderJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;
    private final ManagerJpaRepository managerJpaRepository;
    private final InStockOrderMapper inStockOrderMapper;

    public JpaInStockOrderRepositoryAdapter(
            InStockOrderJpaRepository inStockOrderJpaRepository,
            CustomerJpaRepository customerJpaRepository,
            ManagerJpaRepository managerJpaRepository,
            InStockOrderMapper inStockOrderMapper
    ) {
        this.inStockOrderJpaRepository = inStockOrderJpaRepository;
        this.customerJpaRepository = customerJpaRepository;
        this.managerJpaRepository = managerJpaRepository;
        this.inStockOrderMapper = inStockOrderMapper;
    }

    @Override
    public Optional<InStockOrder> findByIDForUpdate(UUID id) {
        return inStockOrderJpaRepository.findForUpdate(id)
                .map(inStockOrderMapper::toDomain);
    }

    @Override
    public Optional<InStockOrder> findByID(UUID id) {
        return inStockOrderJpaRepository.findById(id)
                .map(inStockOrderMapper::toDomain);
    }

    @Override
    public List<InStockOrder> findAll() {
        return inStockOrderJpaRepository.findAll().stream()
                .map(inStockOrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<InStockOrder> findByCustomerId(UUID customerId) {
        return inStockOrderJpaRepository.findByCustomer_Id(customerId).stream()
                .map(inStockOrderMapper::toDomain)
                .toList();
    }

    @Override
    public InStockOrder save(InStockOrder inStockOrder) {
        CustomerEntity customer = customerJpaRepository.findById(inStockOrder.getCustomerID())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + inStockOrder.getCustomerID()));

        ManagerEntity manager = managerJpaRepository.findById(inStockOrder.getManagerID())
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + inStockOrder.getManagerID()));

        InStockOrderEntity entity = inStockOrderJpaRepository.findById(inStockOrder.getOrderID())
                .orElseGet(InStockOrderEntity::new);

        entity.setId(inStockOrder.getOrderID());
        entity.setCustomer(customer);
        entity.setManager(manager);
        entity.setCarId(inStockOrder.getCarID());
        entity.setStatus(inStockOrder.getOrderStatus());
        entity.setTotalPrice(inStockOrder.getTotalPrice());

        InStockOrderEntity savedEntity = inStockOrderJpaRepository.save(entity);
        return inStockOrderMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByID(UUID id) {
        inStockOrderJpaRepository.deleteById(id);
    }
}
