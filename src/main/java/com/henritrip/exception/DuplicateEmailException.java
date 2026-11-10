package com.henritrip.exception;

/**
 * Exception thrown when attempting to create a user
 * with an email address that already exists.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

}