package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.Car;
import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    Car toDomain(CarEntity entity);

    @Mapping(target = "carModel.id", source = "carModelId")
    CarEntity toEntity(Car car);
}
