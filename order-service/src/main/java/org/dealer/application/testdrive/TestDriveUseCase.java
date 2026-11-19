package org.dealer.application.testdrive;

import org.dealer.domain.enums.TestDriveStatus;
import org.dealer.domain.model.TestDriveRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TestDriveUseCase {
    TestDriveRequest createRequest(UUID customerID, UUID carID, LocalDateTime dataTime);

    TestDriveRequest getByID(UUID requestID);

    List<TestDriveRequest> findAll();
    List<TestDriveRequest> findByCustomer(UUID customerID);
    List<TestDriveRequest> findByStatus(TestDriveStatus status);

    TestDriveRequest approve(UUID requestID, UUID managerID);
    TestDriveRequest reject(UUID requestID, UUID managerID);
    TestDriveRequest cancel(UUID requestID, UUID managerID);
}
