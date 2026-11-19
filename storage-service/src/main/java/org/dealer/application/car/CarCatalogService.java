package org.dealer.application.car;

import lombok.RequiredArgsConstructor;
import org.dealer.application.ports.repository.CarModelRepository;
import org.dealer.application.ports.repository.CarRepository;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.Car;
import org.dealer.domain.model.CarModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CarCatalogService implements CarCatalogUseCase{
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;

    @Override
    public Car getByID(UUID id) {
        if (id == null) throw new DomainValidationException("CarID must be not null");
        return carRepository.findByID(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find car: " + id));
    }

    @Override
    public List<Car> findAvailable(CarFilter filter) {
        return carRepository.findByFilter(filter);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean hasBrand(
            java.util.Map<java.util.UUID, org.dealer.domain.model.CarModel> modelsById,
            java.util.UUID modelId,
            String brand
    ) {
        org.dealer.domain.model.CarModel model = modelsById.get(modelId);
        return model != null
                && model.getBrand() != null
                && brand != null
                && model.getBrand().equalsIgnoreCase(brand);
    }

}
