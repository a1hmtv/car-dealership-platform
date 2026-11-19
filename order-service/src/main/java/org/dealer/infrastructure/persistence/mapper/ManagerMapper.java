package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.Manager;
import org.dealer.infrastructure.persistence.entity.ManagerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    @Mapping(target = "managerID", source = "id")
    @Mapping(target = "name", source = "fullName")
    Manager toDomain(ManagerEntity entity);

    @Mapping(target = "id", source = "managerID")
    @Mapping(target = "fullName", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    ManagerEntity toEntity(Manager manager);
}
