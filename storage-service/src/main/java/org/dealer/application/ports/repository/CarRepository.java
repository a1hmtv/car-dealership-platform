package org.dealer.application.ports.repository;

import org.dealer.application.car.CarFilter;
import org.dealer.domain.model.Car;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository {
    Optional<Car> findByID(UUID id);

    Optional<Car> findByIDForUpdate(UUID id);
    List<Car> findAll();
    Car save(Car car);
    void deleteByID(UUID id);
    List<Car> findByFilter(CarFilter filter);
    List<Car> findAvailableCars();
    Optional<Car> findAvailableCarById(UUID id);
}
