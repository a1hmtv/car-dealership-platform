package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.TestDriveRequest;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.dealer.infrastructure.persistence.entity.TestDriveRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class TestDriveRequestMapper {

    public TestDriveRequest toDomain(TestDriveRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return new TestDriveRequest(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getCarId(),
                entity.getStartTime(),
                entity.getStatus()
        );
    }

    public TestDriveRequestEntity toEntity(
            TestDriveRequest request,
            CustomerEntity customer
    ) {
        if (request == null) {
            return null;
        }

        if (customer == null) {
            throw new IllegalArgumentException("customer must be not null");
        }

        TestDriveRequestEntity entity = new TestDriveRequestEntity();
        entity.setId(request.getId());
        entity.setCustomer(customer);
        entity.setCarId(request.getCarId());
        entity.setStartTime(request.getTestDriveTime());
        entity.setStatus(request.getStatus());
        return entity;
    }
}
