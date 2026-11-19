package org.dealer.infrastructure.persistence.specification;

import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class CarSpecifications {

    public static Specification<CarEntity> hasMinPrice(Integer minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice));
    }

    public static Specification<CarEntity> hasMaxPrice(Integer maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice));
    }

    public static Specification<CarEntity> hasBrand(String brand) {
        return (root, query, cb) ->
                brand == null || brand.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("carModel").get("brand")), brand.toLowerCase());
    }

    public static Specification<CarEntity> hasModelId(UUID modelId) {
        return (root, query, cb) ->
                modelId == null ? null : cb.equal(root.get("carModel").get("id"), modelId);
    }
}
