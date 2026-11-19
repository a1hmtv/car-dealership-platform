package org.dealer.presentation.controller;

import org.dealer.application.car.CarCatalogService;
import org.dealer.application.car.CarFilter;
import org.dealer.domain.model.Car;
import org.dealer.presentation.dto.car.CarFilterRequestDto;
import org.dealer.presentation.dto.car.CarResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarCatalogService carCatalogService;

    public CarController(CarCatalogService carCatalogService) {
        this.carCatalogService = carCatalogService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponseDto getCarById(@PathVariable String id) {
        Car car = carCatalogService.getByID(java.util.UUID.fromString(id));
        return toResponseDto(car);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public java.util.List<CarResponseDto> findAvailableCars(@RequestBody(required = false) CarFilterRequestDto request) {
        CarFilter filter = toCarFilter(request);

        return carCatalogService.findAvailable(filter).stream()
                .map(this::toResponseDto)
                .toList();
    }

    private CarFilter toCarFilter(CarFilterRequestDto request) {
        if (request == null) {
            return null;
        }

        return CarFilter.builder()
                .minPrice(request.getMinPrice() == null ? null : request.getMinPrice().intValue())
                .maxPrice(request.getMaxPrice() == null ? null : request.getMaxPrice().intValue())
                .brand(request.getBrand())
                .modelID(request.getModelId())
                .color(request.getColor())
                .enginePower(request.getEnginePower())
                .engineVolume(request.getEngineVolume())
                .body(request.getBody())
                .drive(request.getDrive())
                .fuel(request.getFuel())
                .transmission(request.getTransmission())
                .build();
    }

    private CarResponseDto toResponseDto(Car car) {
        CarResponseDto dto = new CarResponseDto();
        dto.setId(car.getId());
        dto.setCarModelId(car.getCarModelId());
        dto.setVin(car.getVin());
        dto.setColor(car.getColor());
        dto.setPrice(car.getPrice());
        dto.setBodyType(car.getBodyType() == null ? null : car.getBodyType().name());
        dto.setFuelType(car.getFuelType() == null ? null : car.getFuelType().name());
        dto.setTransmissionType(car.getTransmissionType() == null ? null : car.getTransmissionType().name());
        dto.setDriveType(car.getDriveType() == null ? null : car.getDriveType().name());
        dto.setStatus(car.getStatus() == null ? null : car.getStatus().name());
        dto.setAvailableForTestDrive(car.isAvailableForTestDrive());
        return dto;
    }
}
