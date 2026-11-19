package org.dealer.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dealer.application.assembly.AssemblyOrderUseCase;
import org.dealer.application.ports.repository.AssemblyOrderRepository;
import org.dealer.infrastructure.messaging.event.OrderSentForApprovalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OrderSentForApprovalListener {

    private static final Logger log = LoggerFactory.getLogger(OrderSentForApprovalListener.class);

    private final ObjectMapper objectMapper;
    private final AssemblyOrderUseCase assemblyOrderUseCase;
    private final AssemblyOrderRepository assemblyOrderRepository;

    public OrderSentForApprovalListener(
            ObjectMapper objectMapper,
            AssemblyOrderUseCase assemblyOrderUseCase,
            AssemblyOrderRepository assemblyOrderRepository
    ) {
        this.objectMapper = objectMapper;
        this.assemblyOrderUseCase = assemblyOrderUseCase;
        this.assemblyOrderRepository = assemblyOrderRepository;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_SENT_FOR_APPROVAL_QUEUE)
    @Transactional
    public void handle(String message) {
        OrderSentForApprovalEvent event;
        List<UUID> parts;
        try {
            JsonNode payload = objectMapper.readTree(message);
            parts = extractRequiredPartIds(payload);
            ObjectNode header = ((ObjectNode) payload).deepCopy();
            header.remove(List.of("requiredPartIds", "partIds", "configuration"));
            event = objectMapper.treeToValue(header, OrderSentForApprovalEvent.class);
        } catch (Exception exception) {
            throw new AmqpRejectAndDontRequeueException("Invalid approval event", exception);
        }

        if (event.getOrderId() == null || !"PAID".equals(event.getStatus())
                || (!"IN_STOCK".equals(event.getOrderType()) && !"CUSTOM".equals(event.getOrderType()))) {
            throw new AmqpRejectAndDontRequeueException("Invalid approval event metadata");
        }
        if (assemblyOrderRepository.existsBySourceOrderId(event.getOrderId())) {
            log.info("Skip duplicate ORDER_SENT_FOR_APPROVAL, orderId={}", event.getOrderId());
            return;
        }

        var assemblyOrder = assemblyOrderUseCase.create(
                event.getOrderId(), event.getOrderType(), event.getCarId(), event.getModelId(),
                parts, null, event.getTraceId());
        log.info("Processed approval, orderId={}, assemblyOrderId={}, status={}",
                event.getOrderId(), assemblyOrder.getId(), assemblyOrder.getStatus());
    }

    private List<UUID> extractRequiredPartIds(JsonNode payload) {
        JsonNode partIdsNode = payload.get("requiredPartIds");

        if (partIdsNode == null) {
            partIdsNode = payload.get("partIds");
        }

        if (partIdsNode == null && payload.has("configuration")) {
            JsonNode configurationNode = payload.get("configuration");
            partIdsNode = configurationNode.get("requiredPartIds");

            if (partIdsNode == null) {
                partIdsNode = configurationNode.get("partIds");
            }
        }

        if (partIdsNode == null || !partIdsNode.isArray()) {
            return List.of();
        }

        List<UUID> partIds = new ArrayList<>();
        for (JsonNode partIdNode : partIdsNode) {
            partIds.add(UUID.fromString(partIdNode.asText()));
        }

        return partIds;
    }
}
