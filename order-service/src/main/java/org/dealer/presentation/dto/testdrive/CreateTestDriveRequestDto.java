package org.dealer.presentation.dto.testdrive;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateTestDriveRequestDto {
    public UUID carId;
    public LocalDateTime startTime;
}
