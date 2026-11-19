package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.Customer;
import org.dealer.infrastructure.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "customerID", source = "id")
    @Mapping(target = "name", source = "fullName")
    Customer toDomain(CustomerEntity entity);

    @Mapping(target = "id", source = "customerID")
    @Mapping(target = "fullName", source = "name")
    CustomerEntity toEntity(Customer customer);
}
