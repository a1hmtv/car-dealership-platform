package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.ManagerRepository;
import org.dealer.domain.model.Manager;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.dealer.infrastructure.persistence.jpa.ManagerJpaRepository;
import org.dealer.infrastructure.persistence.mapper.ManagerMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaManagerRepositoryAdapter implements ManagerRepository {
    private final ManagerJpaRepository managerJpaRepository;
    private final ManagerMapper managerMapper;

    public JpaManagerRepositoryAdapter(
            ManagerJpaRepository managerJpaRepository,
            ManagerMapper managerMapper
    ) {
        this.managerJpaRepository = managerJpaRepository;
        this.managerMapper = managerMapper;
    }

    @Override
    public Optional<Manager> findByID(UUID id){
        return managerJpaRepository.findById(id)
                .map(managerMapper::toDomain);
    }

    @Override
    public List<Manager> findAll() {
        return managerJpaRepository.findAll().stream()
                .map(managerMapper::toDomain)
                .toList();
    }

    @Override
    public Manager save(Manager manager) {
        ManagerEntity entity = managerMapper.toEntity(manager);
        ManagerEntity savedEntity = managerJpaRepository.save(entity);
        return managerMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByID(UUID id) {
        managerJpaRepository.deleteById(id);
    }
}
