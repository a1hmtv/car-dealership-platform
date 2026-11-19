package org.dealer;

import org.dealer.application.outbox.OutboxEventUseCase;
import org.dealer.domain.enums.OutboxEventStatus;
import org.dealer.infrastructure.persistence.entity.ProcessedMessageEntity;
import org.dealer.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.dealer.infrastructure.persistence.jpa.ProcessedMessageJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
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
class OrderServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OutboxEventUseCase outboxEventUseCase;

    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Autowired
    private ProcessedMessageJpaRepository processedMessageJpaRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldApplyOrderServiceMigrations() {
        Integer outboxTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'outbox_event'",
                Integer.class
        );
        Integer processedMessageTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'processed_message'",
                Integer.class
        );

        assertThat(outboxTableCount).isEqualTo(1);
        assertThat(processedMessageTableCount).isEqualTo(1);
    }

    @Test
    void shouldStoreOrderSentForApprovalEventInOutbox() {
        UUID orderId = UUID.randomUUID();
        long before = outboxEventJpaRepository.count();

        outboxEventUseCase.createEvent(
                orderId,
                "CUSTOM_ORDER",
                "ORDER_SENT_FOR_APPROVAL",
                Map.of(
                        "orderId", orderId,
                        "orderType", "CUSTOM",
                        "requiredPartIds", java.util.List.of(UUID.randomUUID()),
                        "traceId", UUID.randomUUID().toString()
                )
        );

        assertThat(outboxEventJpaRepository.count()).isEqualTo(before + 1);

        assertThat(outboxEventJpaRepository.findTop20ByStatusInOrderByCreatedAtAsc(
                java.util.List.of(OutboxEventStatus.NEW)
        ))
                .anySatisfy(event -> {
                    assertThat(event.getAggregateId()).isEqualTo(orderId.toString());
                    assertThat(event.getAggregateType()).isEqualTo("CUSTOM_ORDER");
                    assertThat(event.getEventType()).isEqualTo("ORDER_SENT_FOR_APPROVAL");
                    assertThat(event.getPayload()).contains("requiredPartIds");
                    assertThat(event.getPayload()).contains("traceId");
                });
    }

    @Test
    void shouldStoreProcessedMessageForIdempotency() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String traceId = UUID.randomUUID().toString();

        ProcessedMessageEntity processed = new ProcessedMessageEntity();
        processed.setEventId(eventId);
        processed.setEventType("ORDER_APPROVED");
        processed.setOrderId(orderId);
        processed.setTraceId(traceId);
        processed.setProcessedAt(Instant.now());

        processedMessageJpaRepository.save(processed);

        assertThat(processedMessageJpaRepository.existsById(eventId)).isTrue();

        ProcessedMessageEntity saved = processedMessageJpaRepository.findById(eventId).orElseThrow();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getTraceId()).isEqualTo(traceId);
        assertThat(saved.getEventType()).isEqualTo("ORDER_APPROVED");
    }
}
