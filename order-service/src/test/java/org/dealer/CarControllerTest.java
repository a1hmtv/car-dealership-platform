package org.dealer;

import org.dealer.application.car.CarAvailabilityClient;
import org.dealer.application.car.CarNotFoundException;
import org.dealer.application.car.StorageServiceUnavailableException;
import org.dealer.infrastructure.security.SecurityConfig;
import org.dealer.presentation.controller.CarController;
import org.dealer.presentation.controller.CarExceptionHandler;
import org.dealer.presentation.dto.car.CarResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
@Import({SecurityConfig.class, CarExceptionHandler.class})
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarAvailabilityClient carAvailabilityClient;

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnAvailableCarsForUser() throws Exception {
        UUID carId = UUID.randomUUID();

        when(carAvailabilityClient.getAvailableCars()).thenReturn(List.of(testDto(carId)));

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(carId.toString()))
                .andExpect(jsonPath("$[0].vin").value("WBA00000000000001"))
                .andExpect(jsonPath("$[0].status").value("IN_STOCK"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldReturnAvailableCarByIdForManager() throws Exception {
        UUID carId = UUID.randomUUID();

        when(carAvailabilityClient.getAvailableCarById(carId)).thenReturn(testDto(carId));

        mockMvc.perform(get("/api/v1/cars/{id}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.availableForTestDrive").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenCarDoesNotExist() throws Exception {
        UUID carId = UUID.randomUUID();

        when(carAvailabilityClient.getAvailableCarById(carId))
                .thenThrow(new CarNotFoundException("Car not found"));

        mockMvc.perform(get("/api/v1/cars/{id}", carId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Car not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnServiceUnavailableWhenStorageServiceIsUnavailable() throws Exception {
        when(carAvailabilityClient.getAvailableCars())
                .thenThrow(new StorageServiceUnavailableException("StorageService is unavailable"));

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("StorageService is unavailable"));
    }

    @Test
    void shouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE_ADMIN")
    void shouldRejectAuthenticatedUserWithoutAllowedRole() throws Exception {
        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isForbidden());
    }

    private CarResponseDto testDto(UUID id) {
        return new CarResponseDto(
                id,
                UUID.randomUUID(),
                "WBA00000000000001",
                "Black",
                BigDecimal.valueOf(3_350_000),
                "SEDAN",
                "PETROL",
                "AUTOMATIC",
                "RWD",
                "IN_STOCK",
                true
        );
    }
}