package com.reliaquest.api.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.model.EmployeeDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for Employee API
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"employee.api.base-url=http://localhost:8112/", "employee.api.timeout=10000"})
class EmployeeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllEmployees_Integration() {
        // This test requires the mock server to be running
        // When
        ResponseEntity<List<EmployeeDTO>> response = restTemplate.exchange(
                "/api/v1/employee", HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeDTO>>() {});

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
    }

    @Test
    void getHighestSalary_Integration() {
        // This test requires the mock server to be running
        // When
        ResponseEntity<Integer> response = restTemplate.getForEntity("/api/v1/employee/highestSalary", Integer.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Integer);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Integration() {
        // This test requires the mock server to be running
        // When
        ResponseEntity<List<String>> response = restTemplate.exchange(
                "/api/v1/employee/topTenHighestEarningEmployeeNames",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
    }

    @Test
    void createEmployee_Integration() {
        // Given
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .employeeName("Integration Test Employee")
                .employeeSalary(65000)
                .employeeAge(28)
                .employeeTitle("QA Engineer")
                .build();

        // When
        ResponseEntity<EmployeeDTO> response =
                restTemplate.postForEntity("/api/v1/employee", request, EmployeeDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Employee", response.getBody().getName());
        assertEquals(65000, response.getBody().getSalary());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void searchEmployeesByName_Integration() {
        // This test requires the mock server to be running
        // When
        ResponseEntity<List<EmployeeDTO>> response = restTemplate.exchange(
                "/api/v1/employee/search/a",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EmployeeDTO>>() {});

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
    }

    @Test
    void getNonExistentEmployee_Integration() {
        // Given - A valid UUID format that doesn't exist
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/employee/" + nonExistentId, String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getEmployeeWithInvalidUUID_Integration() {
        // Given - Invalid UUID format
        String invalidId = "invalid-uuid-format";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/employee/" + invalidId, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
