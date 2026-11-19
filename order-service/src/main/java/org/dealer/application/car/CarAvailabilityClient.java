package org.dealer.application.car;

import com.example.grpc.caravailability.Car;
import com.example.grpc.caravailability.CarAvailabilityServiceGrpc;
import com.example.grpc.caravailability.GetAvailableCarByIdRequest;
import com.example.grpc.caravailability.GetAvailableCarsRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.dealer.presentation.dto.car.CarResponseDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CarAvailabilityClient {

    @GrpcClient("storage-service")
    private CarAvailabilityServiceGrpc.CarAvailabilityServiceBlockingStub carStub;

    public List<CarResponseDto> getAvailableCars() {
        try {
            log.info("gRPC request: get available cars");

            var response = carStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

            log.info("gRPC response: received {} cars", response.getCarsCount());

            return response.getCarsList().stream()
                    .map(this::toDto)
                    .toList();

        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception);
        }
    }

    public CarResponseDto getAvailableCarById(UUID id) {
        try {
            log.info("gRPC request: get available car by id {}", id);

            var response = carStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getAvailableCarById(
                            GetAvailableCarByIdRequest.newBuilder()
                                    .setId(id.toString())
                                    .build()
                    );

            log.info("gRPC response: car {} received", id);

            return toDto(response.getCar());

        } catch (StatusRuntimeException exception) {
            throw mapGrpcException(exception);
        }
    }

    private RuntimeException mapGrpcException(StatusRuntimeException exception) {
        Status.Code code = exception.getStatus().getCode();

        log.error("gRPC error from StorageService: {}", code, exception);

        if (code == Status.Code.NOT_FOUND) {
            return new CarNotFoundException("Car not found");
        }

        if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
            return new StorageServiceUnavailableException("StorageService is unavailable");
        }

        return new StorageServiceUnavailableException("StorageService error");
    }

    private CarResponseDto toDto(Car car) {
        return new CarResponseDto(
                UUID.fromString(car.getId()),
                UUID.fromString(car.getCarModelId()),
                car.getVin(),
                car.getColor(),
                BigDecimal.valueOf(car.getPrice()),
                car.getBodyType(),
                car.getFuelType(),
                car.getTransmissionType(),
                car.getDriveType(),
                car.getStatus(),
                car.getAvailableForTestDrive()
        );
    }
}