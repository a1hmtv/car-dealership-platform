package org.dealer.domain.model;

import org.dealer.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartStockTest {
    private PartStock stock(int total, int reserved, boolean removed) {
        return new PartStock(UUID.randomUUID(), UUID.randomUUID(), total, reserved, removed);
    }

    @Test
    void reserveReducesAvailableQuantity() {
        PartStock stock = stock(10, 0, false);
        stock.reserve(4);
        assertEquals(6, stock.getAvailableQuantity());
        assertEquals(4, stock.getReservedQuantity());
        assertEquals(10, stock.getTotalQuantity());
    }

    @Test
    void releaseRestoresAvailability() {
        PartStock stock = stock(10, 4, false);
        stock.release(2);
        assertEquals(8, stock.getAvailableQuantity());
        assertEquals(2, stock.getReservedQuantity());
        assertEquals(10, stock.getTotalQuantity());
    }

    @Test
    void consumeReducesTotalAndReserved() {
        PartStock stock = stock(10, 4, false);
        stock.consume(4);
        assertEquals(6, stock.getTotalQuantity());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(6, stock.getAvailableQuantity());
    }

    @Test
    void invalidOperationsDoNotChangeStock() {
        PartStock stock = stock(10, 4, false);
        assertThrows(DomainValidationException.class, () -> stock.reserve(7));
        assertThrows(DomainValidationException.class, () -> stock.release(5));
        assertThrows(DomainValidationException.class, () -> stock.consume(0));
        assertEquals(10, stock.getTotalQuantity());
        assertEquals(4, stock.getReservedQuantity());
    }

    @Test
    void removedStockCannotBeReserved() {
        PartStock stock = stock(10, 0, true);
        assertFalse(stock.canReserve(1));
        assertThrows(DomainValidationException.class, () -> stock.reserve(1));
    }
}
