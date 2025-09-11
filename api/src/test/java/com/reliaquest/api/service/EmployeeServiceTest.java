package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeCreateRequestDTO;
import com.reliaquest.api.model.EmployeeDTO;
import com.reliaquest.api.model.ExternalEmployeeDTO;
import com.reliaquest.api.service.impl.EmployeeServiceImpl;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeApiClient employeeApiClient;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getAllEmployees_Success() {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createTestEmployee(UUID.randomUUID(), "John Doe", 50000),
                createTestEmployee(UUID.randomUUID(), "Jane Smith", 60000));
        ApiResponse<List<EmployeeDTO>> apiResponse = new ApiResponse<>(employees, "Success");

        when(employeeApiClient.getAllEmployees()).thenReturn(apiResponse);

        // When
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Then
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    void getAllEmployees_HttpClientErrorException() {
        // Given
        when(employeeApiClient.getAllEmployees())
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void getEmployeeById_Success() {
        // Given
        UUID employeeId = UUID.randomUUID();
        EmployeeDTO employee = createTestEmployee(employeeId, "John Doe", 50000);
        ApiResponse<EmployeeDTO> apiResponse = new ApiResponse<>(employee, "Success");

        when(employeeApiClient.getEmployeeById(employeeId)).thenReturn(apiResponse);

        // When
        EmployeeDTO result = employeeService.getEmployeeById(employeeId);

        // Then
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals(50000, result.getSalary());
    }

    @Test
    void getEmployeeById_NotFound() {
        // Given
        UUID employeeId = UUID.randomUUID();
        when(employeeApiClient.getEmployeeById(employeeId))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createTestEmployee(UUID.randomUUID(), "John Doe", 50000),
                createTestEmployee(UUID.randomUUID(), "Jane Smith", 60000),
                createTestEmployee(UUID.randomUUID(), "John Johnson", 55000));
        ApiResponse<List<EmployeeDTO>> apiResponse = new ApiResponse<>(employees, "Success");

        when(employeeApiClient.getAllEmployees()).thenReturn(apiResponse);

        // When
        List<EmployeeDTO> result = employeeService.getEmployeesByNameSearch("John");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(emp -> emp.getName().contains("John")));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createTestEmployee(UUID.randomUUID(), "John Doe", 50000),
                createTestEmployee(UUID.randomUUID(), "Jane Smith", 75000),
                createTestEmployee(UUID.randomUUID(), "Bob Johnson", 60000));
        ApiResponse<List<EmployeeDTO>> apiResponse = new ApiResponse<>(employees, "Success");

        when(employeeApiClient.getAllEmployees()).thenReturn(apiResponse);

        // When
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertEquals(75000, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createTestEmployee(UUID.randomUUID(), "John Doe", 50000),
                createTestEmployee(UUID.randomUUID(), "Jane Smith", 75000),
                createTestEmployee(UUID.randomUUID(), "Bob Johnson", 60000),
                createTestEmployee(UUID.randomUUID(), "Alice Brown", 80000));
        ApiResponse<List<EmployeeDTO>> apiResponse = new ApiResponse<>(employees, "Success");

        when(employeeApiClient.getAllEmployees()).thenReturn(apiResponse);

        // When
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertEquals(4, result.size());
        assertEquals("Alice Brown", result.get(0)); // Highest salary
        assertEquals("Jane Smith", result.get(1)); // Second highest
        assertEquals("Bob Johnson", result.get(2)); // Third highest
        assertEquals("John Doe", result.get(3)); // Lowest salary
    }

    @Test
    void createEmployee_Success() {
        // Given
        EmployeeCreateRequestDTO request = EmployeeCreateRequestDTO.builder()
                .employeeName("New Employee")
                .employeeSalary(55000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .build();

        EmployeeDTO createdEmployee = createTestEmployee(UUID.randomUUID(), "New Employee", 55000);
        ApiResponse<EmployeeDTO> apiResponse = new ApiResponse<>(createdEmployee, "Success");

        when(employeeApiClient.createEmployee(any(ExternalEmployeeDTO.class))).thenReturn(apiResponse);

        // When
        EmployeeDTO result = employeeService.createEmployee(request);

        // Then
        assertNotNull(result.getId());
        assertEquals("New Employee", result.getName());
        assertEquals(55000, result.getSalary());
    }

    @Test
    void deleteEmployeeById_Success() {
        // Given
        UUID employeeId = UUID.randomUUID();
        EmployeeDTO employee = createTestEmployee(employeeId, "John Doe", 50000);
        ApiResponse<EmployeeDTO> getApiResponse = new ApiResponse<>(employee, "Success");
        ApiResponse<Boolean> deleteApiResponse = new ApiResponse<>(true, "Success");

        when(employeeApiClient.getEmployeeById(employeeId)).thenReturn(getApiResponse);
        when(employeeApiClient.deleteEmployeeByName(eq("John Doe"), any())).thenReturn(deleteApiResponse);

        // When
        String result = employeeService.deleteEmployeeById(employeeId);

        // Then
        assertEquals("John Doe", result);
    }

    @Test
    void getAllEmployees_RateLimitExceeded() {
        // Given
        when(employeeApiClient.getAllEmployees())
                .thenThrow(new com.reliaquest.api.exception.RateLimitExceededException("Rate limit exceeded"));

        // When & Then
        assertThrows(
                com.reliaquest.api.exception.RateLimitExceededException.class, () -> employeeService.getAllEmployees());
    }

    private EmployeeDTO createTestEmployee(UUID id, String name, Integer salary) {
        return EmployeeDTO.builder()
                .id(id)
                .name(name)
                .salary(salary)
                .age(30)
                .title("Test Title")
                .email("test@company.com")
                .build();
    }
}
