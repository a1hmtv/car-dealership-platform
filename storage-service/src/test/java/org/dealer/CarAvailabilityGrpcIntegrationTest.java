package org.dealer;

import com.example.grpc.caravailability.CarAvailabilityServiceGrpc;
import com.example.grpc.caravailability.GetAvailableCarByIdRequest;
import com.example.grpc.caravailability.GetAvailableCarsRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.dealer.domain.enums.BodyType;
import org.dealer.domain.enums.CarStatus;
import org.dealer.domain.enums.DriveType;
import org.dealer.domain.enums.FuelType;
import org.dealer.domain.enums.TransmissionType;
import org.dealer.infrastructure.persistence.entity.CarEntity;
import org.dealer.infrastructure.persistence.entity.CarModelEntity;
import org.dealer.infrastructure.persistence.jpa.CarJpaRepository;
import org.dealer.infrastructure.persistence.jpa.CarModelJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(properties = {
        "grpc.server.port=19090",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "dealer.outbox.enabled=false",
        "spring.rabbitmq.dynamic=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/dealer-realm",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/dealer-realm/protocol/openid-connect/certs"
})
class CarAvailabilityGrpcIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("storage_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private CarJpaRepository carJpaRepository;

    @Autowired
    private CarModelJpaRepository carModelJpaRepository;

    private ManagedChannel channel;
    private CarAvailabilityServiceGrpc.CarAvailabilityServiceBlockingStub stub;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        carJpaRepository.deleteAll();

        channel = ManagedChannelBuilder.forAddress("localhost", 19090)
                .usePlaintext()
                .build();
        stub = CarAvailabilityServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        channel.shutdownNow();
        channel.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void shouldReturnOnlyCarsAvailableForSale() {
        CarEntity availableCar = saveCar(CarStatus.IN_STOCK);
        saveCar(CarStatus.SOLD);

        var response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertThat(response.getCarsList())
                .hasSize(1)
                .first()
                .satisfies(car -> {
                    assertThat(car.getId()).isEqualTo(availableCar.getId().toString());
                    assertThat(car.getStatus()).isEqualTo(CarStatus.IN_STOCK.name());
                    assertThat(car.getVin()).isEqualTo(availableCar.getVin());
                    assertThat(car.getColor()).isEqualTo(availableCar.getColor());
                    assertThat(car.getAvailableForTestDrive()).isTrue();
                });
    }

    @Test
    void shouldReturnAvailableCarById() {
        CarEntity availableCar = saveCar(CarStatus.IN_STOCK);

        var response = stub.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder()
                        .setId(availableCar.getId().toString())
                        .build()
        );

        assertThat(response.getCar().getId()).isEqualTo(availableCar.getId().toString());
        assertThat(response.getCar().getStatus()).isEqualTo(CarStatus.IN_STOCK.name());
    }

    @Test
    void shouldReturnNotFoundForMissingOrUnavailableCar() {
        CarEntity soldCar = saveCar(CarStatus.SOLD);

        assertThatThrownBy(() -> stub.getAvailableCarById(
                GetAvailableCarByIdRequest.newBuilder()
                        .setId(soldCar.getId().toString())
                        .build()
        ))
                .isInstanceOfSatisfying(StatusRuntimeException.class, exception ->
                        assertThat(exception.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND)
                );
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAvailableCars() {
        saveCar(CarStatus.SOLD);

        var response = stub.getAvailableCars(GetAvailableCarsRequest.newBuilder().build());

        assertThat(response.getCarsList()).isEmpty();
    }

    private CarEntity saveCar(CarStatus status) {
        CarModelEntity model = new CarModelEntity();
        model.setBrand("BMW");
        model.setModelName("320i");
        model.setBasePrice(BigDecimal.valueOf(3_200_000));
        model = carModelJpaRepository.save(model);

        CarEntity car = new CarEntity();
        car.setCarModel(model);
        car.setVin("VIN" + UUID.randomUUID().toString().replace("-", "").substring(0, 14));
        car.setColor("Black");
        car.setPrice(BigDecimal.valueOf(3_350_000));
        car.setBodyType(BodyType.SEDAN);
        car.setFuelType(FuelType.PETROL);
        car.setTransmissionType(TransmissionType.AUTOMATIC);
        car.setDriveType(DriveType.RWD);
        car.setStatus(status);
        car.setAvailableForTestDrive(true);
        return carJpaRepository.save(car);
    }
}
