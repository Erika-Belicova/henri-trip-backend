package com.henritrip.exception;

/**
 * Exception thrown when an activity with the specified identifier
 * is not found in the system.
 */
public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(String message) {
        super(message);
    }

}
