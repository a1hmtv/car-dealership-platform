package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.enums.TestDriveStatus;
import org.dealer.domain.exceptions.DomainValidationException;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TestDriveRequest {
    private final UUID id;
    private final UUID customerId;
    private final UUID carId;
    private final LocalDateTime testDriveTime;
    private TestDriveStatus status;

    public TestDriveRequest(UUID id,
                            UUID customerId,
                            UUID carId,
                            LocalDateTime testDriveTime,
                            TestDriveStatus status) {
        if (id == null) throw new DomainValidationException("id must be not null");
        if (customerId == null) throw new DomainValidationException("customerId must be not null");
        if (carId == null) throw new DomainValidationException("carId must be not null");
        if (testDriveTime == null) throw new DomainValidationException("testDriveTime must be not null");
        if (status == null) throw new DomainValidationException("status must be not null");

        this.id = id;
        this.customerId = customerId;
        this.carId = carId;
        this.testDriveTime = testDriveTime;
        this.status = status;
    }

    public void changeStatus(TestDriveStatus newStatus) {
        if (newStatus == null) throw new DomainValidationException("newStatus must be not null");
        this.status = newStatus;
    }
}