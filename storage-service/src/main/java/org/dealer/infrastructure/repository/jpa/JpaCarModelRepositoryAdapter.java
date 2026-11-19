package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.CarModelRepository;
import org.dealer.domain.model.CarModel;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.dealer.infrastructure.persistence.jpa.CarModelJpaRepository;
import org.dealer.infrastructure.persistence.mapper.CarModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCarModelRepositoryAdapter implements CarModelRepository {

    private final CarModelJpaRepository carModelJpaRepository;
    private final CarModelMapper carModelMapper;

    public JpaCarModelRepositoryAdapter(
            CarModelJpaRepository carModelJpaRepository,
            CarModelMapper carModelMapper
    ) {
        this.carModelJpaRepository = carModelJpaRepository;
        this.carModelMapper = carModelMapper;
    }

    @Override
    public Optional<CarModel> findByID(UUID id) {
        return carModelJpaRepository.findById(id)
                .map(carModelMapper::toDomain);
    }

    @Override
    public List<CarModel> findAll() {
        return carModelJpaRepository.findAll().stream()
                .map(carModelMapper::toDomain)
                .toList();
    }

    @Override
    public CarModel save(CarModel carModel) {
        CarModelEntity entity = carModelMapper.toEntity(carModel);
        CarModelEntity saved = carModelJpaRepository.save(entity);
        return carModelMapper.toDomain(saved);
    }

    @Override
    public void deleteByID(UUID id) {
        carModelJpaRepository.deleteById(id);
    }
}
