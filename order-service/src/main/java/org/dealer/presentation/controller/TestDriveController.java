package org.dealer.presentation.controller;

import org.dealer.application.ports.repository.CustomerRepository;
import org.dealer.application.ports.repository.ManagerRepository;
import org.dealer.application.testdrive.TestDriveUseCase;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.Customer;
import org.dealer.domain.model.Manager;
import org.dealer.domain.model.TestDriveRequest;
import org.dealer.presentation.dto.testdrive.CreateTestDriveRequestDto;
import org.dealer.presentation.dto.testdrive.TestDriveRequestResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test-drives")
public class TestDriveController {

    private final TestDriveUseCase testDriveUseCase;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;

    public TestDriveController(
            TestDriveUseCase testDriveUseCase,
            CustomerRepository customerRepository,
            ManagerRepository managerRepository
    ) {
        this.testDriveUseCase = testDriveUseCase;
        this.customerRepository = customerRepository;
        this.managerRepository = managerRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TestDriveRequestResponseDto createRequest(@RequestBody CreateTestDriveRequestDto request) {
        UUID currentCustomerId = getCurrentCustomerId();

        TestDriveRequest created = testDriveUseCase.createRequest(
                currentCustomerId,
                request.getCarId(),
                request.getStartTime()
        );
        return toDto(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public TestDriveRequestResponseDto getById(@PathVariable UUID id) {
        return toDto(testDriveUseCase.getByID(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<TestDriveRequestResponseDto> getAll() {
        return testDriveUseCase.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponseDto approve(@PathVariable UUID id) {
        return toDto(testDriveUseCase.approve(id, getCurrentManagerId()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponseDto reject(@PathVariable UUID id) {
        return toDto(testDriveUseCase.reject(id, getCurrentManagerId()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponseDto cancel(@PathVariable UUID id) {
        return toDto(testDriveUseCase.cancel(id, getCurrentManagerId()));
    }

    private UUID getCurrentCustomerId() {
        String email = getCurrentUserEmail();

        Customer customer = customerRepository.findAll().stream()
                .filter(item -> item.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found for authenticated user email: " + email
                ));

        return customer.getCustomerID();
    }

    private UUID getCurrentManagerId() {
        String email = getCurrentUserEmail();

        Manager manager = managerRepository.findAll().stream()
                .filter(item -> item.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Manager not found for authenticated user email: " + email
                ));

        return manager.getManagerID();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authenticated JWT user not found in SecurityContext");
        }

        return jwt.getClaimAsString("email");
    }

    private TestDriveRequestResponseDto toDto(TestDriveRequest request) {
        TestDriveRequestResponseDto dto = new TestDriveRequestResponseDto();
        dto.setId(request.getId());
        dto.setCustomerId(request.getCustomerId());
        dto.setCarId(request.getCarId());
        dto.setStartTime(request.getTestDriveTime());
        dto.setStatus(request.getStatus().name());
        return dto;
    }
}
