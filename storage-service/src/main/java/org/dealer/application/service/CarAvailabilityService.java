package org.dealer.application.service;

import com.example.grpc.caravailability.Car;
import com.example.grpc.caravailability.CarAvailabilityServiceGrpc;
import com.example.grpc.caravailability.GetAvailableCarByIdRequest;
import com.example.grpc.caravailability.GetAvailableCarByIdResponse;
import com.example.grpc.caravailability.GetAvailableCarsRequest;
import com.example.grpc.caravailability.GetAvailableCarsResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.dealer.domain.enums.CarStatus;
import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.dealer.infrastructure.persistence.jpa.CarJpaRepository;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CarAvailabilityService extends CarAvailabilityServiceGrpc.CarAvailabilityServiceImplBase {

    private final CarJpaRepository carJpaRepository;

    @Override
    public void getAvailableCars(
            GetAvailableCarsRequest request,
            StreamObserver<GetAvailableCarsResponse> responseObserver
    ) {
        log.info("gRPC request: get available cars");

        var cars = carJpaRepository.findAllByStatus(CarStatus.IN_STOCK);

        var response = GetAvailableCarsResponse.newBuilder()
                .addAllCars(
                        cars.stream()
                                .map(this::toGrpcCar)
                                .toList()
                )
                .build();

        log.info("gRPC response: found {} available cars", response.getCarsCount());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableCarById(
            GetAvailableCarByIdRequest request,
            StreamObserver<GetAvailableCarByIdResponse> responseObserver
    ) {
        log.info("gRPC request: get available car by id={}", request.getId());

        UUID id;

        try {
            id = UUID.fromString(request.getId());
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid car id")
                            .asRuntimeException()
            );
            return;
        }

        var car = carJpaRepository.findByIdAndStatus(id, CarStatus.IN_STOCK);

        if (car.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Available car not found")
                            .asRuntimeException()
            );
            return;
        }

        var response = GetAvailableCarByIdResponse.newBuilder()
                .setCar(toGrpcCar(car.get()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Car toGrpcCar(CarEntity entity) {
        return Car.newBuilder()
                .setId(entity.getId().toString())
                .setCarModelId(entity.getCarModel().getId().toString())
                .setVin(entity.getVin())
                .setColor(entity.getColor())
                .setPrice(entity.getPrice().doubleValue())
                .setBodyType(entity.getBodyType().name())
                .setFuelType(entity.getFuelType().name())
                .setTransmissionType(entity.getTransmissionType().name())
                .setDriveType(entity.getDriveType().name())
                .setStatus(entity.getStatus().name())
                .setAvailableForTestDrive(entity.isAvailableForTestDrive())
                .build();
    }
}