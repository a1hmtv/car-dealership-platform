package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.car.CarFilter;
import org.dealer.application.ports.repository.CarRepository;
import org.dealer.domain.enums.CarStatus;
import org.dealer.domain.model.Car;
import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.dealer.infrastructure.persistence.jpa.CarJpaRepository;
import org.dealer.infrastructure.persistence.mapper.CarMapper;
import org.dealer.infrastructure.persistence.specification.CarSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCarRepositoryAdapter implements CarRepository {

    private final CarJpaRepository carJpaRepository;
    private final CarMapper carMapper;

    public JpaCarRepositoryAdapter(
            CarJpaRepository carJpaRepository,
            CarMapper carMapper
    ) {
        this.carJpaRepository = carJpaRepository;
        this.carMapper = carMapper;
    }

    @Override
    public Optional<Car> findByIDForUpdate(UUID id) {
        return carJpaRepository.findForUpdate(id)
                .map(carMapper::toDomain);
    }

    @Override
    public Optional<Car> findByID(UUID id) {
        return carJpaRepository.findById(id)
                .map(carMapper::toDomain);
    }

    @Override
    public List<Car> findAll() {
        return carJpaRepository.findAll().stream()
                .map(carMapper::toDomain)
                .toList();
    }

    @Override
    public Car save(Car car) {
        CarEntity entity = carMapper.toEntity(car);
        CarEntity saved = carJpaRepository.save(entity);
        return carMapper.toDomain(saved);
    }

    @Override
    public void deleteByID(UUID id) {
        carJpaRepository.deleteById(id);
    }

    @Override
    public List<Car> findByFilter(CarFilter filter) {
        Specification<CarEntity> spec = Specification.where(null);

        if (filter != null) {
            spec = spec.and(CarSpecifications.hasMinPrice(filter.getMinPrice()));
            spec = spec.and(CarSpecifications.hasMaxPrice(filter.getMaxPrice()));
            spec = spec.and(CarSpecifications.hasBrand(filter.getBrand()));
            spec = spec.and(CarSpecifications.hasModelId(filter.getModelID()));
        }

        return carJpaRepository.findAll(spec).stream()
                .map(carMapper::toDomain)
                .toList();
    }

    @Override
    public List<Car> findAvailableCars() {
        return carJpaRepository.findAllByStatus(CarStatus.IN_STOCK).stream()
                .map(carMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Car> findAvailableCarById(UUID id) {
        return carJpaRepository.findByIdAndStatus(id, CarStatus.IN_STOCK)
                .map(carMapper::toDomain);
    }
}
