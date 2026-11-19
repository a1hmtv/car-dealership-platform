package org.dealer.application.ports.repository;

import org.dealer.domain.model.Part;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartRepository {
    Optional<Part> findByID(UUID name);
    List<Part> findAll();
    Part save(Part part);
    void deleteByID(UUID name);
}
