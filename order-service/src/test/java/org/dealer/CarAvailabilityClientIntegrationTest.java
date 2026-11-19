package org.dealer;

import com.example.grpc.caravailability.Car;
import com.example.grpc.caravailability.CarAvailabilityServiceGrpc;
import com.example.grpc.caravailability.GetAvailableCarByIdRequest;
import com.example.grpc.caravailability.GetAvailableCarByIdResponse;
import com.example.grpc.caravailability.GetAvailableCarsRequest;
import com.example.grpc.caravailability.GetAvailableCarsResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.dealer.application.car.CarAvailabilityClient;
import org.dealer.application.car.CarNotFoundException;
import org.dealer.application.car.StorageServiceUnavailableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(properties = {
        "grpc.client.storage-service.address=static://localhost:19091",
        "grpc.client.storage-service.negotiation-type=plaintext",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "dealer.outbox.enabled=false",
        "spring.rabbitmq.dynamic=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/dealer-realm",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/dealer-realm/protocol/openid-connect/certs"
})
class CarAvailabilityClientIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    private static final FakeStorageGrpcService fakeStorageService = new FakeStorageGrpcService();
    private static Server server;

    @Autowired
    private CarAvailabilityClient carAvailabilityClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void startFakeStorageService() throws IOException {
        server = ServerBuilder.forPort(19091)
                .addService(fakeStorageService)
                .build()
                .start();
    }

    @AfterAll
    static void stopFakeStorageService() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void shouldReceiveAvailableCarsFromStorageService() {
        UUID carId = UUID.randomUUID();
        fakeStorageService.mode = FakeMode.SUCCESS;
        fakeStorageService.cars = List.of(testCar(carId));

        var cars = carAvailabilityClient.getAvailableCars();

        assertThat(cars)
                .hasSize(1)
                .first()
                .satisfies(car -> {
                    assertThat(car.id()).isEqualTo(carId);
                    assertThat(car.vin()).isEqualTo("WBA00000000000001");
                    assertThat(car.status()).isEqualTo("IN_STOCK");
                });
    }

    @Test
    void shouldReturnEmptyListWhenStorageServiceReturnsNoCars() {
        fakeStorageService.mode = FakeMode.SUCCESS;
        fakeStorageService.cars = List.of();

        assertThat(carAvailabilityClient.getAvailableCars()).isEmpty();
    }

    @Test
    void shouldMapGrpcNotFoundToDomainException() {
        fakeStorageService.mode = FakeMode.NOT_FOUND;

        assertThatThrownBy(() -> carAvailabilityClient.getAvailableCarById(UUID.randomUUID()))
                .isInstanceOf(CarNotFoundException.class)
                .hasMessage("Car not found");
    }

    @Test
    void shouldMapGrpcUnavailableToServiceUnavailableException() {
        fakeStorageService.mode = FakeMode.UNAVAILABLE;

        assertThatThrownBy(() -> carAvailabilityClient.getAvailableCars())
                .isInstanceOf(StorageServiceUnavailableException.class)
                .hasMessage("StorageService is unavailable");
    }

    @Test
    void shouldMapGrpcTimeoutToServiceUnavailableException() {
        fakeStorageService.mode = FakeMode.TIMEOUT;

        assertThatThrownBy(() -> carAvailabilityClient.getAvailableCars())
                .isInstanceOf(StorageServiceUnavailableException.class)
                .hasMessage("StorageService is unavailable");
    }

    private static Car testCar(UUID id) {
        return Car.newBuilder()
                .setId(id.toString())
                .setCarModelId(UUID.randomUUID().toString())
                .setVin("WBA00000000000001")
                .setColor("Black")
                .setPrice(3_350_000)
                .setBodyType("SEDAN")
                .setFuelType("PETROL")
                .setTransmissionType("AUTOMATIC")
                .setDriveType("RWD")
                .setStatus("IN_STOCK")
                .setAvailableForTestDrive(true)
                .build();
    }

    private enum FakeMode {
        SUCCESS,
        NOT_FOUND,
        UNAVAILABLE,
        TIMEOUT
    }

    private static class FakeStorageGrpcService extends CarAvailabilityServiceGrpc.CarAvailabilityServiceImplBase {
        private volatile FakeMode mode = FakeMode.SUCCESS;
        private volatile List<Car> cars = List.of();

        @Override
        public void getAvailableCars(
                GetAvailableCarsRequest request,
                StreamObserver<GetAvailableCarsResponse> responseObserver
        ) {
            switch (mode) {
                case UNAVAILABLE -> responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
                case TIMEOUT -> sleepLongerThanClientDeadline();
                default -> {
                    responseObserver.onNext(GetAvailableCarsResponse.newBuilder().addAllCars(cars).build());
                    responseObserver.onCompleted();
                }
            }
        }

        @Override
        public void getAvailableCarById(
                GetAvailableCarByIdRequest request,
                StreamObserver<GetAvailableCarByIdResponse> responseObserver
        ) {
            switch (mode) {
                case NOT_FOUND -> responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
                case UNAVAILABLE -> responseObserver.onError(Status.UNAVAILABLE.asRuntimeException());
                case TIMEOUT -> sleepLongerThanClientDeadline();
                default -> {
                    Car car = cars.stream()
                            .filter(candidate -> candidate.getId().equals(request.getId()))
                            .findFirst()
                            .orElseGet(() -> testCar(UUID.fromString(request.getId())));

                    responseObserver.onNext(GetAvailableCarByIdResponse.newBuilder().setCar(car).build());
                    responseObserver.onCompleted();
                }
            }
        }

        private void sleepLongerThanClientDeadline() {
            try {
                Thread.sleep(4_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
