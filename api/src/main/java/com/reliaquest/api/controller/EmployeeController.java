package com.reliaquest.api.controller;

import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.model.EmployeeDTO;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for employee operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<EmployeeDTO, EmployeeCreateRequest> {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        log.info("GET /api/v1/employee - Fetching all employees");
        long startTs = System.currentTimeMillis();
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        log.info(
                "Successfully retrieved {} employees in {} ms", employees.size(), System.currentTimeMillis() - startTs);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(String searchString) {
        log.info("GET /api/v1/employee/search/{} - Searching employees by name", searchString);

        List<EmployeeDTO> employees = employeeService.getEmployeesByNameSearch(searchString);

        log.info("Found {} employees matching search term: {}", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<EmployeeDTO> getEmployeeById(String id) {
        log.info("GET /api/v1/employee/{} - Fetching employee by ID", id);

        // Validate UUID format
        UUID employeeId = getUuidFromString(id);

        EmployeeDTO employee = employeeService.getEmployeeById(employeeId);

        log.info("Successfully retrieved employee: {}", employee.getName());
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("GET /api/v1/employee/highestSalary - Finding highest salary");

        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        log.info("Highest salary found: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("GET /api/v1/employee/topTenHighestEarningEmployeeNames - Finding top 10 earners");

        List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();

        log.info("Found {} top earning employees", topEarners.size());
        return ResponseEntity.ok(topEarners);
    }

    @Override
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid EmployeeCreateRequest employeeInput) {
        log.info("POST /api/v1/employee - Creating new employee: {}", employeeInput.getEmployeeName());

        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeInput);

        log.info("Successfully created employee: {} with ID: {}", createdEmployee.getName(), createdEmployee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("DELETE /api/v1/employee/{} - Deleting employee by ID", id);

        UUID employeeId = getUuidFromString(id);

        String deletedEmployeeName = employeeService.deleteEmployeeById(employeeId);

        log.info("Successfully deleted employee: {}", deletedEmployeeName);
        return ResponseEntity.ok(deletedEmployeeName);
    }

    @NotNull private static UUID getUuidFromString(String id) {
        // Validate UUID format
        UUID employeeId;
        try {
            employeeId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format provided: {}", id);
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
        return employeeId;
    }
}
