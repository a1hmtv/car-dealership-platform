package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.CarModel;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarModelMapper {

    @Mapping(target = "availableOptions", expression = "java(java.util.Map.of())")
    @Mapping(target = "name", source = "modelName")
    CarModel toDomain(CarModelEntity entity);

    @Mapping(target = "modelName", source = "name")
    CarModelEntity toEntity(CarModel model);
}
