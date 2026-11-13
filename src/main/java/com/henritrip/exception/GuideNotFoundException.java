package com.henritrip.exception;

/**
 * Exception thrown when a guide with the specified identifier
 * is not found in the system.
 */
public class GuideNotFoundException extends RuntimeException {

    public GuideNotFoundException(String message) {
        super(message);
    }

}
