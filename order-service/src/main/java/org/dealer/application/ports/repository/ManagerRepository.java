package org.dealer.application.ports.repository;

import org.dealer.domain.model.Manager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository {
    Optional<Manager> findByID(UUID id);
    List<Manager> findAll();
    Manager save(Manager manager);
    void deleteByID(UUID id);
}
