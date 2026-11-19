package org.dealer.application.car;

import org.dealer.domain.model.Car;

import java.util.List;
import java.util.UUID;

public interface CarCatalogUseCase {
    Car getByID(UUID id);
    List<Car> findAvailable(CarFilter filter);
}
