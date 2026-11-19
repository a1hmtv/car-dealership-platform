package org.dealer.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.dealer.application.ports.repository.CustomOrderRepository;
import org.dealer.application.ports.repository.InStockOrderRepository;
import org.dealer.domain.enums.OrderStatusCustom;
import org.dealer.domain.enums.OrderStatusInStock;
import org.dealer.domain.exceptions.EntityNotFoundException;
import org.dealer.domain.model.CustomOrder;
import org.dealer.domain.model.InStockOrder;
import org.dealer.infrastructure.messaging.event.OrderResultEvent;
import org.dealer.infrastructure.persistence.entity.ProcessedMessageEntity;
import org.dealer.infrastructure.persistence.jpa.ProcessedMessageJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class StorageOrderResultListener {

    private static final Logger log = LoggerFactory.getLogger(StorageOrderResultListener.class);

    private final ObjectMapper objectMapper;
    private final InStockOrderRepository inStockOrderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final ProcessedMessageJpaRepository processedMessageJpaRepository;

    public StorageOrderResultListener(
            ObjectMapper objectMapper,
            InStockOrderRepository inStockOrderRepository,
            CustomOrderRepository customOrderRepository,
            ProcessedMessageJpaRepository processedMessageJpaRepository
    ) {
        this.objectMapper = objectMapper;
        this.inStockOrderRepository = inStockOrderRepository;
        this.customOrderRepository = customOrderRepository;
        this.processedMessageJpaRepository = processedMessageJpaRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_APPROVED_QUEUE)
    @Transactional
    public void handleApproved(String message) {
        handle(message, true);
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_REJECTED_QUEUE)
    @Transactional
    public void handleRejected(String message) {
        handle(message, false);
    }

    private void handle(String message, boolean approved) {
        OrderResultEvent event = null;

        try {
            event = objectMapper.readValue(message, OrderResultEvent.class);
            String eventType = approved ? "ORDER_APPROVED" : "ORDER_REJECTED";

            if (event == null || event.getOrderId() == null || !eventType.equals(event.getStatus())
                    || (!"IN_STOCK".equals(event.getOrderType()) && !"CUSTOM".equals(event.getOrderType()))) {
                throw new AmqpRejectAndDontRequeueException(
                        "Invalid storage result event");
            }

            UUID eventId = resolveEventId(event);
            putTraceId(event);

            log.info(
                    "Received {} eventId={}, orderId={}, orderType={}, assemblyOrderId={}",
                    eventType,
                    eventId,
                    event.getOrderId(),
                    event.getOrderType(),
                    event.getAssemblyOrderId()
            );

            if (processedMessageJpaRepository.existsById(eventId)) {
                log.info("Skip duplicate {} eventId={}, orderId={}", eventType, eventId, event.getOrderId());
                return;
            }

            if ("IN_STOCK".equals(event.getOrderType())) {
                updateInStockOrder(event, approved);
            } else if ("CUSTOM".equals(event.getOrderType())) {
                updateCustomOrder(event, approved);
            } else {
                throw new IllegalArgumentException("Unknown order type: " + event.getOrderType());
            }

            markProcessed(event, eventId, eventType);

            log.info(
                    "Processed or ignored {} eventId={}, orderId={}",
                    eventType,
                    eventId,
                    event.getOrderId()
            );
        } catch (JsonProcessingException exception) {
            throw new AmqpRejectAndDontRequeueException("Invalid result event JSON", exception);
        } catch (AmqpRejectAndDontRequeueException exception) {
            throw exception;
        } catch (Exception exception) {
            UUID orderId = event == null ? null : event.getOrderId();
            log.error("Failed to process storage order result event, orderId={}", orderId, exception);
            throw new IllegalStateException("Failed to process storage order result event", exception);
        } finally {
            MDC.remove("traceId");
        }
    }

    private void updateInStockOrder(OrderResultEvent event, boolean approved) {
        InStockOrder order = inStockOrderRepository.findByIDForUpdate(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("InStockOrder not found: " + event.getOrderId()));

        if (order.getOrderStatus() != OrderStatusInStock.PAID) {
            log.info("Ignore stale result for orderId={}, status={}",
                    event.getOrderId(), order.getOrderStatus());
            return;
        }

        if (approved) {
            order.changeStatus(OrderStatusInStock.READY_FOR_PICKUP);
        } else {
            order.changeStatus(OrderStatusInStock.CANCELLED);
        }

        inStockOrderRepository.save(order);
    }

    private void updateCustomOrder(OrderResultEvent event, boolean approved) {
        CustomOrder order = customOrderRepository.findByIDForUpdate(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("CustomOrder not found: " + event.getOrderId()));

        if (order.getOrderStatus() != OrderStatusCustom.PAID
                && order.getOrderStatus() != OrderStatusCustom.AWAITING_DELIVERY) {
            log.info("Ignore stale result for orderId={}, status={}",
                    event.getOrderId(), order.getOrderStatus());
            return;
        }

        if (approved) {
            order.changeStatus(OrderStatusCustom.READY_FOR_PICKUP);
        } else {
            order.changeStatus(OrderStatusCustom.CANCELLED);
        }

        customOrderRepository.save(order);
    }

    private void markProcessed(OrderResultEvent event, UUID eventId, String eventType) {
        ProcessedMessageEntity processed = new ProcessedMessageEntity();
        processed.setEventId(eventId);
        processed.setEventType(eventType);
        processed.setOrderId(event.getOrderId());
        processed.setTraceId(event.getTraceId());
        processed.setProcessedAt(Instant.now());

        processedMessageJpaRepository.save(processed);
    }

    private UUID resolveEventId(OrderResultEvent event) {
        if (event.getEventId() != null) {
            return event.getEventId();
        }

        String fallbackKey = event.getOrderId()
                + "|" + event.getOrderType()
                + "|" + event.getAssemblyOrderId()
                + "|" + event.getStatus();

        return UUID.nameUUIDFromBytes(fallbackKey.getBytes(StandardCharsets.UTF_8));
    }

    private void putTraceId(OrderResultEvent event) {
        if (event.getTraceId() != null && !event.getTraceId().isBlank()) {
            MDC.put("traceId", event.getTraceId());
        }
    }
}
