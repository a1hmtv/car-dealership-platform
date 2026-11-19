package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.domain.model.Customer;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.dealer.infrastructure.persistence.mapper.CustomerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerMapper customerMapper;

    public JpaCustomerRepositoryAdapter(
            CustomerJpaRepository customerJpaRepository,
            CustomerMapper customerMapper
    ) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerMapper = customerMapper;
    }


    @Override
    public Optional<Customer> findByID(UUID id) {
        return customerJpaRepository.findById(id)
                .map(customerMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll().stream()
                .map(customerMapper::toDomain)
                .toList();
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customerMapper.toEntity(customer);
        CustomerEntity savedEntity = customerJpaRepository.save(entity);
        return customerMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByID(UUID id) {
        customerJpaRepository.deleteById(id);
    }
}
