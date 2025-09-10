package com.reliaquest.api.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for retry event logging
 */
@Slf4j
@Configuration
public class RetryEventConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

        // Add event listeners for all retry instances
        retryRegistry.getEventPublisher().onEntryAdded(entryAddedEvent -> {
            Retry retry = entryAddedEvent.getAddedEntry();
            retry.getEventPublisher().onRetry(event -> {
                log.warn(
                        "Retry attempt {} for operation '{}' due to: {} - waiting {}ms before next attempt",
                        event.getNumberOfRetryAttempts(),
                        retry.getName(),
                        event.getLastThrowable().getClass().getSimpleName() + ": "
                                + event.getLastThrowable().getMessage(),
                        retry.getRetryConfig().getIntervalFunction().apply(event.getNumberOfRetryAttempts()));
            });

            retry.getEventPublisher().onSuccess(event -> {
                if (event.getNumberOfRetryAttempts() > 0) {
                    log.info(
                            "Operation '{}' succeeded after {} retry attempts",
                            retry.getName(),
                            event.getNumberOfRetryAttempts());
                }
            });

            retry.getEventPublisher().onError(event -> {
                log.error(
                        "Operation '{}' failed after {} attempts. Final error: {}",
                        retry.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getClass().getSimpleName() + ": "
                                + event.getLastThrowable().getMessage());
            });
        });

        return retryRegistry;
    }
}
