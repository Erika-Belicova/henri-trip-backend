package com.henritrip.exception;

/**
 * Exception thrown when a guide day with the specified identifier
 * is not found in the system.
 */
public class GuideDayNotFoundException extends RuntimeException {

    public GuideDayNotFoundException(String message) {
        super(message);
    }

}
