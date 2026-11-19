package org.dealer.infrastructure.messaging.event;

import java.util.UUID;

public class OrderResultEvent {

    private UUID eventId;
    private UUID orderId;
    private String orderType;
    private UUID assemblyOrderId;
    private String status;
    private String traceId;

    public OrderResultEvent() {
    }

    public OrderResultEvent(
            UUID eventId,
            UUID orderId,
            String orderType,
            UUID assemblyOrderId,
            String status,
            String traceId
    ) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.orderType = orderType;
        this.assemblyOrderId = assemblyOrderId;
        this.status = status;
        this.traceId = traceId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public UUID getAssemblyOrderId() {
        return assemblyOrderId;
    }

    public void setAssemblyOrderId(UUID assemblyOrderId) {
        this.assemblyOrderId = assemblyOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
