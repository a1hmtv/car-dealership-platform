package org.dealer.application.ports.repository;

import org.dealer.domain.model.CarModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelRepository {
    Optional<CarModel> findByID(UUID id);
    List<CarModel> findAll();
    CarModel save(CarModel carModel);
    void deleteByID(UUID id);
}