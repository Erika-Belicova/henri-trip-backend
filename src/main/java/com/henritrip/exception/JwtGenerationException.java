package com.henritrip.exception;

/**
 * Exception thrown when an error occurs during JWT generation.
 */
public class JwtGenerationException extends RuntimeException {

    public JwtGenerationException(String message) {
        super(message);
    }

}
