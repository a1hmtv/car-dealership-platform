package org.dealer.infrastructure.repository.jpa;

import org.dealer.application.ports.repository.TestDriveRequestRepository;
import org.dealer.domain.enums.TestDriveStatus;
import org.dealer.domain.model.TestDriveRequest;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.TestDriveRequestEntity;
import org.dealer.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.dealer.infrastructure.persistence.jpa.TestDriveRequestJpaRepository;
import org.dealer.infrastructure.persistence.mapper.TestDriveRequestMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTestDriveRequestRepositoryAdapter implements TestDriveRequestRepository {

    private final TestDriveRequestJpaRepository testDriveRequestJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;
    private final TestDriveRequestMapper testDriveRequestMapper;

    public JpaTestDriveRequestRepositoryAdapter(
            TestDriveRequestJpaRepository testDriveRequestJpaRepository,
            CustomerJpaRepository customerJpaRepository,
            TestDriveRequestMapper testDriveRequestMapper
    ) {
        this.testDriveRequestJpaRepository = testDriveRequestJpaRepository;
        this.customerJpaRepository = customerJpaRepository;
        this.testDriveRequestMapper = testDriveRequestMapper;
    }

    @Override
    public Optional<TestDriveRequest> findByID(UUID id) {
        return testDriveRequestJpaRepository.findById(id)
                .map(testDriveRequestMapper::toDomain);
    }

    @Override
    public List<TestDriveRequest> findAll() {
        return testDriveRequestJpaRepository.findAll().stream()
                .map(testDriveRequestMapper::toDomain)
                .toList();
    }

    @Override
    public TestDriveRequest save(TestDriveRequest request) {
        CustomerEntity customer = customerJpaRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.getCustomerId()));

        TestDriveRequestEntity entity = testDriveRequestMapper.toEntity(request, customer);
        TestDriveRequestEntity savedEntity = testDriveRequestJpaRepository.save(entity);

        return testDriveRequestMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByID(UUID id) {
        testDriveRequestJpaRepository.deleteById(id);
    }

    @Override
    public List<TestDriveRequest> findByCustomer(UUID customerID) {
        return testDriveRequestJpaRepository.findAll().stream()
                .filter(entity -> entity.getCustomer().getId().equals(customerID))
                .map(testDriveRequestMapper::toDomain)
                .toList();
    }

    @Override
    public List<TestDriveRequest> findByStatus(TestDriveStatus status) {
        return testDriveRequestJpaRepository.findAll().stream()
                .filter(entity -> entity.getStatus() == status)
                .map(testDriveRequestMapper::toDomain)
                .toList();
    }
}
