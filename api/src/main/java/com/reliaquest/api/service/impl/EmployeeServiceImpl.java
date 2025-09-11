package com.reliaquest.api.service.impl;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalApiException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeCreateRequestDTO;
import com.reliaquest.api.model.EmployeeDTO;
import com.reliaquest.api.model.ExternalEmployeeDTO;
import com.reliaquest.api.service.EmployeeService;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Service for managing employee operations with the EmployeeApiClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final String EMPLOYEE_SERVER = "employee-server";

    private final EmployeeApiClient employeeApiClient;

    /**
     * Retrieves all employees from the mock API
     */
    @Retry(name = EMPLOYEE_SERVER)
    public List<EmployeeDTO> getAllEmployees() {
        log.debug("Attempting to fetch all employees from API");

        ApiResponse<List<EmployeeDTO>> response = employeeApiClient.getAllEmployees();

        if (response != null && response.getData() != null) {
            List<EmployeeDTO> employees = response.getData();
            log.debug("Successfully retrieved {} employees", employees.size());
            return employees;
        }

        throw new ExternalApiException("Empty response from employee API");
    }

    /**
     * Retrieves a single employee by ID
     */
    @Retry(name = EMPLOYEE_SERVER)
    public EmployeeDTO getEmployeeById(UUID id) {
        log.debug("Attempting to fetch employee with ID: {}", id);

        try {
            ApiResponse<EmployeeDTO> response = employeeApiClient.getEmployeeById(id);

            if (response != null && response.getData() != null) {
                EmployeeDTO employee = response.getData();
                log.debug("Successfully retrieved employee: {}", employee.getName());
                return employee;
            }

            throw new EmployeeNotFoundException("Employee not found with ID: " + id);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee not found with ID: {}", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        }
    }

    /**
     * Searches for employees by name fragment
     */
    @Retry(name = EMPLOYEE_SERVER)
    public List<EmployeeDTO> getEmployeesByNameSearch(String searchString) {
        log.debug("Searching employees with name containing: {}", searchString);

        List<EmployeeDTO> allEmployees = getAllEmployees();

        List<EmployeeDTO> matchingEmployees = allEmployees.stream()
                .filter(employee -> employee.getName() != null
                        && employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Found {} employees matching search term: {}", matchingEmployees.size(), searchString);
        return matchingEmployees;
    }

    /**
     * Gets the highest salary among all employees
     */
    @Retry(name = EMPLOYEE_SERVER)
    public Integer getHighestSalaryOfEmployees() {
        log.debug("Finding highest salary among all employees");

        List<EmployeeDTO> allEmployees = getAllEmployees();

        Integer highestSalary = allEmployees.stream()
                .filter(employee -> employee.getSalary() != null)
                .mapToInt(EmployeeDTO::getSalary)
                .max()
                .orElse(0);

        log.debug("Highest salary found: {}", highestSalary);
        return highestSalary;
    }

    /**
     * Gets the names of the top 10 highest earning employees
     */
    @Retry(name = EMPLOYEE_SERVER)
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.debug("Finding top 10 highest earning employee names");

        List<EmployeeDTO> allEmployees = getAllEmployees();

        List<String> topTenNames = allEmployees.stream()
                .filter(employee -> employee.getSalary() != null && employee.getName() != null)
                .sorted(Comparator.comparing(EmployeeDTO::getSalary).reversed())
                .limit(10)
                .map(EmployeeDTO::getName)
                .collect(Collectors.toList());

        log.debug("Found top {} highest earning employees", topTenNames.size());
        return topTenNames;
    }

    /**
     * Creates a new employee
     */
    @Retry(name = EMPLOYEE_SERVER)
    public EmployeeDTO createEmployee(EmployeeCreateRequestDTO request) {
        log.debug("Attempting to create new employee: {}", request.getEmployeeName());

        ExternalEmployeeDTO employeeForAPI = ExternalEmployeeDTO.fromCreateRequest(request);

        ApiResponse<EmployeeDTO> response = employeeApiClient.createEmployee(employeeForAPI);

        if (response != null && response.getData() != null) {
            EmployeeDTO createdEmployee = response.getData();
            log.debug(
                    "Successfully created employee: {} with ID: {}",
                    createdEmployee.getName(),
                    createdEmployee.getId());
            return createdEmployee;
        }

        throw new ExternalApiException("Failed to create employee - empty response");
    }

    /**
     * Deletes an employee by ID and returns the employee's name
     */
    @Retry(name = EMPLOYEE_SERVER)
    public String deleteEmployeeById(UUID id) {
        log.debug("Attempting to delete employee with ID: {}", id);

        // First, get the employee to retrieve the name
        EmployeeDTO employee = getEmployeeById(id);
        String employeeName = employee.getName();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", employeeName);

        ApiResponse<Boolean> response = employeeApiClient.deleteEmployeeByName(employeeName, requestBody);

        if (response != null && Boolean.TRUE.equals(response.getData())) {
            log.debug("Successfully deleted employee: {}", employeeName);
            return employeeName;
        }

        throw new ExternalApiException("Failed to delete employee - operation not confirmed");
    }
}
