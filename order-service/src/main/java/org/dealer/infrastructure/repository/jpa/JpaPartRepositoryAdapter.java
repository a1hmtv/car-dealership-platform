package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.PartRepository;
import org.dealer.domain.model.Part;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.dealer.infrastructure.persistence.entity.PartCompatibleCarModelEntity;
import org.dealer.infrastructure.persistence.entity.PartEntity;
import org.dealer.infrastructure.persistence.jpa.PartCompatibleCarModelJpaRepository;
import org.dealer.infrastructure.persistence.jpa.PartJpaRepository;
import org.dealer.infrastructure.persistence.mapper.PartMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class JpaPartRepositoryAdapter implements PartRepository {

    private final PartJpaRepository partJpaRepository;
    private final PartCompatibleCarModelJpaRepository partCompatibleCarModelJpaRepository;
    private final PartMapper partMapper;

    public JpaPartRepositoryAdapter(
            PartJpaRepository partJpaRepository,
            PartCompatibleCarModelJpaRepository partCompatibleCarModelJpaRepository,
            PartMapper partMapper
    ) {
        this.partJpaRepository = partJpaRepository;
        this.partCompatibleCarModelJpaRepository = partCompatibleCarModelJpaRepository;
        this.partMapper = partMapper;
    }

    @Override
    public Optional<Part> findByID(UUID id) {
        return partJpaRepository.findById(id)
                .map(entity -> partMapper.toDomain(entity, loadCompatibleCarIds(entity.getId())));
    }

    @Override
    public List<Part> findAll() {
        return partJpaRepository.findAll().stream()
                .map(entity -> partMapper.toDomain(entity, loadCompatibleCarIds(entity.getId())))
                .toList();
    }

    @Override
    public Part save(Part part) {
        PartEntity entity = partMapper.toEntity(part);
        PartEntity savedEntity = partJpaRepository.save(entity);

        partCompatibleCarModelJpaRepository.deleteByPart_Id(savedEntity.getId());

        for (UUID carModelId : part.getCompatibleCarIds()) {
            PartCompatibleCarModelEntity link = new PartCompatibleCarModelEntity();
            link.setId(UUID.randomUUID());
            link.setPart(savedEntity);

            CarModelEntity carModel = new CarModelEntity();
            carModel.setId(carModelId);
            link.setCarModel(carModel);

            partCompatibleCarModelJpaRepository.save(link);
        }

        return partMapper.toDomain(savedEntity, loadCompatibleCarIds(savedEntity.getId()));
    }

    @Override
    public void deleteByID(UUID id) {
        partCompatibleCarModelJpaRepository.deleteByPart_Id(id);
        partJpaRepository.deleteById(id);
    }

    private Set<UUID> loadCompatibleCarIds(UUID partId) {
        return partCompatibleCarModelJpaRepository.findByPart_Id(partId).stream()
                .map(link -> link.getCarModel().getId())
                .collect(java.util.stream.Collectors.toSet());
    }
}
