package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.model.EmployeeDTO;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEmployees_Success() throws Exception {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(
                createTestEmployee(UUID.randomUUID(), "John Doe", 50000),
                createTestEmployee(UUID.randomUUID(), "Jane Smith", 60000));
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$[1].employee_name").value("Jane Smith"));
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        // Given
        List<EmployeeDTO> employees = Arrays.asList(createTestEmployee(UUID.randomUUID(), "John Doe", 50000));
        when(employeeService.getEmployeesByNameSearch("John")).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        EmployeeDTO employee = createTestEmployee(employeeId, "John Doe", 50000);
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.employee_name").value("John Doe"))
                .andExpect(jsonPath("$.employee_salary").value(50000));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        when(employeeService.getEmployeeById(employeeId))
                .thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        // When & Then
        mockMvc.perform(get("/api/v1/employee/" + employeeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Employee not found with ID: " + employeeId));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Given
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(75000);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("75000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Given
        List<String> topEarners = Arrays.asList("Alice Brown", "Jane Smith", "Bob Johnson");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Alice Brown"))
                .andExpect(jsonPath("$[1]").value("Jane Smith"))
                .andExpect(jsonPath("$[2]").value("Bob Johnson"));
    }

    @Test
    void createEmployee_Success() throws Exception {
        // Given
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .employeeName("New Employee")
                .employeeSalary(55000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .build();

        EmployeeDTO createdEmployee = createTestEmployee(UUID.randomUUID(), "New Employee", 55000);
        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(createdEmployee);

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employee_name").value("New Employee"))
                .andExpect(jsonPath("$.employee_salary").value(55000));
    }

    @Test
    void createEmployee_ValidationError() throws Exception {
        // Given - Invalid request with blank name
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .employeeName("") // Invalid - blank name
                .employeeSalary(55000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_ValidationError_InvalidAge() throws Exception {
        // Given - Invalid request with age below minimum
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .employeeName("Test Employee")
                .employeeSalary(55000)
                .employeeAge(15) // Invalid - below minimum age
                .employeeTitle("Developer")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_ValidationError_InvalidSalary() throws Exception {
        // Given - Invalid request with zero salary
        EmployeeCreateRequest request = EmployeeCreateRequest.builder()
                .employeeName("Test Employee")
                .employeeSalary(0) // Invalid - must be greater than zero
                .employeeAge(25)
                .employeeTitle("Developer")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn("John Doe");

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("John Doe"));
    }

    @Test
    void deleteEmployeeById_NotFound() throws Exception {
        // Given
        UUID employeeId = UUID.randomUUID();
        when(employeeService.deleteEmployeeById(employeeId))
                .thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/" + employeeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Employee not found with ID: " + employeeId));
    }

    @Test
    void getEmployeeById_InvalidUUID() throws Exception {
        // Given - Invalid UUID format
        String invalidId = "invalid-uuid-format";

        // When & Then
        mockMvc.perform(get("/api/v1/employee/" + invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid input: Invalid UUID format: " + invalidId));
    }

    @Test
    void deleteEmployeeById_InvalidUUID() throws Exception {
        // Given - Invalid UUID format
        String invalidId = "invalid-uuid-format";

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/" + invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid input: Invalid UUID format: " + invalidId));
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
