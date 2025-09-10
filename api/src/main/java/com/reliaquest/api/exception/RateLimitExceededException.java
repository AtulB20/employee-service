package com.reliaquest.api.exception;

/**
 * Exception thrown when rate limit (HTTP 429) is encountered from external API
 * This exception is designed to trigger retry mechanisms
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
