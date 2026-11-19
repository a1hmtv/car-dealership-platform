package org.dealer;

import org.dealer.application.outbox.StorageOutboxEventUseCase;
import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.infrastructure.messaging.RabbitMqConfig;
import org.dealer.infrastructure.persistence.jpa.StorageOutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "dealer.outbox.enabled=false",
        "spring.rabbitmq.dynamic=false",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/dealer-realm",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/dealer-realm/protocol/openid-connect/certs"
})
class StorageServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("storage_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StorageOutboxEventUseCase storageOutboxEventUseCase;

    @Autowired
    private StorageOutboxEventJpaRepository storageOutboxEventJpaRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldApplyStorageServiceMigrations() {
        Integer assemblyOrderTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'assembly_order'",
                Integer.class
        );
        Integer storageOutboxTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'storage_outbox_event'",
                Integer.class
        );

        assertThat(assemblyOrderTableCount).isEqualTo(1);
        assertThat(storageOutboxTableCount).isEqualTo(1);
    }

    @Test
    void shouldStoreOrderResultEventInStorageOutbox() {
        UUID assemblyOrderId = UUID.randomUUID();
        UUID sourceOrderId = UUID.randomUUID();
        String traceId = UUID.randomUUID().toString();

        long before = storageOutboxEventJpaRepository.count();

        storageOutboxEventUseCase.createEvent(
                assemblyOrderId,
                "ASSEMBLY_ORDER",
                "ORDER_APPROVED",
                RabbitMqConfig.ORDER_EXCHANGE,
                RabbitMqConfig.ORDER_APPROVED_ROUTING_KEY,
                Map.of(
                        "eventId", UUID.randomUUID(),
                        "orderId", sourceOrderId,
                        "orderType", "CUSTOM",
                        "assemblyOrderId", assemblyOrderId,
                        "status", "ORDER_APPROVED",
                        "traceId", traceId
                )
        );

        assertThat(storageOutboxEventJpaRepository.count()).isEqualTo(before + 1);

        assertThat(storageOutboxEventJpaRepository.findTop20ByStatusInOrderByCreatedAtAsc(
                java.util.List.of(OutboxEventStatus.NEW)
        ))
                .anySatisfy(event -> {
                    assertThat(event.getAggregateId()).isEqualTo(assemblyOrderId.toString());
                    assertThat(event.getAggregateType()).isEqualTo("ASSEMBLY_ORDER");
                    assertThat(event.getEventType()).isEqualTo("ORDER_APPROVED");
                    assertThat(event.getExchangeName()).isEqualTo(RabbitMqConfig.ORDER_EXCHANGE);
                    assertThat(event.getRoutingKey()).isEqualTo(RabbitMqConfig.ORDER_APPROVED_ROUTING_KEY);
                    assertThat(event.getPayload()).contains("eventId");
                    assertThat(event.getPayload()).contains("traceId");
                    assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.NEW);
                });
    }
}
