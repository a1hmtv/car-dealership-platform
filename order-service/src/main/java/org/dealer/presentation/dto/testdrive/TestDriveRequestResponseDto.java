package org.dealer.presentation.dto.testdrive;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TestDriveRequestResponseDto {
    public UUID id;
    public UUID customerId;
    public UUID carId;
    public LocalDateTime startTime;
    public String status;
}
