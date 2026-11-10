package com.henritrip.exception;

/**
 * Exception thrown when a resource conflict occurs,
 * such as attempting to create a resource that already exists.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

}