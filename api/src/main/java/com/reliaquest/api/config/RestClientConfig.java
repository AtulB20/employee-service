package com.reliaquest.api.config;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration for REST client to communicate with mock employee API
 */
@Configuration
public class RestClientConfig {

    @Value("${employee.api.timeout:5000}")
    private int timeout;

    @Value("${employee.api.base-url}")
    private String baseUrl;

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    /**
     * Creates a RestClient configured with rate limiting detection.
     * When HTTP 429 (Too Many Requests) is received, it throws
     * RateLimitExceededException
     * which triggers the retry mechanism configured in Resilience4j.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(clientHttpRequestFactory())
                .defaultStatusHandler(status -> status.value() == 429, (request, response) -> {
                    throw new RateLimitExceededException("Rate limit exceeded (HTTP 429) - triggering retry mechanism");
                })
                .build();
    }

    @Bean
    public EmployeeApiClient employeeApiClient(RestClient restClient) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(EmployeeApiClient.class);
    }
}
