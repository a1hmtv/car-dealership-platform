package org.dealer.infrastructure.persistence.mapper;

import org.dealer.domain.model.Part;
import org.dealer.infrastructure.persistence.entity.PartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {HashSet.class, Set.class})
public interface PartMapper {

    @Mapping(target = "partId", source = "entity.id")
    @Mapping(target = "name", source = "entity.name")
    @Mapping(target = "componentType", source = "entity.componentType")
    @Mapping(target = "priceDelta", source = "entity.priceDelta")
    @Mapping(
            target = "compatibleCarIds",
            expression = "java(compatibleCarIds == null ? new HashSet<>() : new HashSet<>(compatibleCarIds))"
    )
    Part toDomain(PartEntity entity, Set<UUID> compatibleCarIds);

    default Part toDomain(PartEntity entity) {
        return toDomain(entity, Set.of());
    }

    @Mapping(target = "id", source = "partId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "componentType", source = "componentType")
    @Mapping(target = "priceDelta", source = "priceDelta")
    PartEntity toEntity(Part part);
}
