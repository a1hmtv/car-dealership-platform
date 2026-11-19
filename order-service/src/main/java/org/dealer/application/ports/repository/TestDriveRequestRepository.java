package org.dealer.application.ports.repository;

import org.dealer.domain.enums.TestDriveStatus;
import org.dealer.domain.model.TestDriveRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestRepository {
    Optional<TestDriveRequest> findByID(UUID requestID);
    List<TestDriveRequest> findAll();
    List<TestDriveRequest> findByCustomer(UUID customerID);
    List<TestDriveRequest> findByStatus(TestDriveStatus status);
    TestDriveRequest save(TestDriveRequest request);
    void deleteByID(UUID requestID);
}
