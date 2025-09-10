package com.reliaquest.api.exception;

/**
 * Exception thrown when external API calls fail
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }
}
