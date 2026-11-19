package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.AssemblyOrderRepository;
import org.dealer.domain.model.AssemblyOrder;
import org.dealer.infrastructure.persistence.entity.AssemblyOrderEntity;
import org.dealer.infrastructure.persistence.jpa.AssemblyOrderJpaRepository;
import org.dealer.infrastructure.persistence.mapper.AssemblyOrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAssemblyOrderRepositoryAdapter implements AssemblyOrderRepository {

    private final AssemblyOrderJpaRepository assemblyOrderJpaRepository;
    private final AssemblyOrderMapper assemblyOrderMapper;

    public JpaAssemblyOrderRepositoryAdapter(
            AssemblyOrderJpaRepository assemblyOrderJpaRepository,
            AssemblyOrderMapper assemblyOrderMapper
    ) {
        this.assemblyOrderJpaRepository = assemblyOrderJpaRepository;
        this.assemblyOrderMapper = assemblyOrderMapper;
    }

    @Override
    public AssemblyOrder save(AssemblyOrder assemblyOrder) {
        AssemblyOrderEntity entity = assemblyOrderMapper.toEntity(assemblyOrder);
        AssemblyOrderEntity savedEntity = assemblyOrderJpaRepository.save(entity);
        return assemblyOrderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AssemblyOrder> findByIdForUpdate(UUID id) {
        return assemblyOrderJpaRepository.findForUpdate(id)
                .map(assemblyOrderMapper::toDomain);
    }

    @Override
    public Optional<AssemblyOrder> findById(UUID id) {
        return assemblyOrderJpaRepository.findById(id)
                .map(assemblyOrderMapper::toDomain);
    }

    @Override
    public List<AssemblyOrder> findAll() {
        return assemblyOrderJpaRepository.findAll().stream()
                .map(assemblyOrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<AssemblyOrder> findActive() {
        return assemblyOrderJpaRepository.findByRemovedFalse().stream()
                .map(assemblyOrderMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySourceOrderId(UUID sourceOrderId) {
        return assemblyOrderJpaRepository.existsBySourceOrderId(sourceOrderId);
    }

    @Override
    public void deleteById(UUID id) {
        assemblyOrderJpaRepository.deleteById(id);
    }
}
