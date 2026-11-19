package org.dealer.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.dealer.application.car.CarAvailabilityClient;
import org.dealer.presentation.dto.car.CarResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class CarController {

    private final CarAvailabilityClient carAvailabilityClient;

    @GetMapping
    public List<CarResponseDto> getAvailableCars() {
        return carAvailabilityClient.getAvailableCars();
    }

    @GetMapping("/{id}")
    public CarResponseDto getAvailableCarById(@PathVariable UUID id) {
        return carAvailabilityClient.getAvailableCarById(id);
    }
}