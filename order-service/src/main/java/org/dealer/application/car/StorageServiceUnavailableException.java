package org.dealer.application.car;

public class StorageServiceUnavailableException extends RuntimeException {
    public StorageServiceUnavailableException(String message) {
        super(message);
    }
}