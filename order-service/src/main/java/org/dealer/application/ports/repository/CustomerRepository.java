package org.dealer.application.ports.repository;

import org.dealer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Optional<Customer> findByID(UUID id);
    List<Customer> findAll();
    Customer save(Customer customer);
    void deleteByID(UUID id);
}
