package org.dealer.application.testdrive;

import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.application.ports.repository.ManagerRepository;
import org.dealer.application.ports.repository.TestDriveRequestRepository;
import org.dealer.domain.enums.TestDriveStatus;
import org.dealer.domain.exceptions.DomainValidationException;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.TestDriveRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TestDriveService implements TestDriveUseCase {

    private final TestDriveRequestRepository testDriveRepository;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;

    public TestDriveService(
            TestDriveRequestRepository testDriveRepository,
            CustomerRepository customerRepository,
            ManagerRepository managerRepository
    ) {
        if (testDriveRepository == null) {
            throw new DomainValidationException("TestDriveRepository must be not null");
        }
        if (customerRepository == null) {
            throw new DomainValidationException("CustomerRepository must be not null");
        }
        if (managerRepository == null) {
            throw new DomainValidationException("ManagerRepository must be not null");
        }

        this.testDriveRepository = testDriveRepository;
        this.customerRepository = customerRepository;
        this.managerRepository = managerRepository;
    }

    @Override
    public TestDriveRequest createRequest(UUID customerID, UUID carID, LocalDateTime dateTime) {
        if (customerID == null) {
            throw new DomainValidationException("CustomerID must be not null");
        }
        if (carID == null) {
            throw new DomainValidationException("CarID must be not null");
        }
        if (dateTime == null) {
            throw new DomainValidationException("DateTime must be not null");
        }

        customerRepository.findByID(customerID)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerID));

        TestDriveRequest request = new TestDriveRequest(
                UUID.randomUUID(),
                customerID,
                carID,
                dateTime,
                TestDriveStatus.CREATED
        );

        testDriveRepository.save(request);
        return request;
    }

    @Override
    public TestDriveRequest getByID(UUID requestID) {
        if (requestID == null) {
            throw new DomainValidationException("RequestID must be not null");
        }

        return testDriveRepository.findByID(requestID)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest not found: " + requestID));
    }

    @Override
    public List<TestDriveRequest> findAll() {
        return testDriveRepository.findAll();
    }

    @Override
    public List<TestDriveRequest> findByCustomer(UUID customerID) {
        if (customerID == null) {
            throw new DomainValidationException("CustomerID must be not null");
        }

        return testDriveRepository.findByCustomer(customerID);
    }

    @Override
    public List<TestDriveRequest> findByStatus(TestDriveStatus status) {
        if (status == null) {
            throw new DomainValidationException("Status must be not null");
        }

        return testDriveRepository.findByStatus(status);
    }

    @Override
    public TestDriveRequest approve(UUID requestID, UUID managerID) {
        return changeStatus(requestID, managerID, TestDriveStatus.APPROVED);
    }

    @Override
    public TestDriveRequest reject(UUID requestID, UUID managerID) {
        return changeStatus(requestID, managerID, TestDriveStatus.REJECTED);
    }

    @Override
    public TestDriveRequest cancel(UUID requestID, UUID managerID) {
        return changeStatus(requestID, managerID, TestDriveStatus.CANCELLED);
    }

    private TestDriveRequest changeStatus(UUID requestID, UUID managerID, TestDriveStatus newStatus) {
        if (requestID == null) {
            throw new DomainValidationException("RequestID must be not null");
        }
        if (managerID == null) {
            throw new DomainValidationException("ManagerID must be not null");
        }
        if (newStatus == null) {
            throw new DomainValidationException("Status must be not null");
        }

        managerRepository.findByID(managerID)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found: " + managerID));

        TestDriveRequest request = getByID(requestID);
        TestDriveStatus current = request.getStatus();

        if (!isAllowedTransition(current, newStatus)) {
            throw new DomainValidationException("Illegal status transition: " + current + " -> " + newStatus);
        }

        request.changeStatus(newStatus);
        testDriveRepository.save(request);
        return request;
    }

    private boolean isAllowedTransition(TestDriveStatus from, TestDriveStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return from != TestDriveStatus.CANCELLED;
    }
}
