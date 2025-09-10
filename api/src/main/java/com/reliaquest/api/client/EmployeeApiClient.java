package com.reliaquest.api.client;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeDTO;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP Interface for communicating with the mock Employee API
 */
@HttpExchange(url = "/api/v1/employee", accept = "application/json")
public interface EmployeeApiClient {

    @GetExchange("/")
    ApiResponse<List<EmployeeDTO>> getAllEmployees();

    @GetExchange("/{id}")
    ApiResponse<EmployeeDTO> getEmployeeById(@PathVariable UUID id);

    @PostExchange("/")
    ApiResponse<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeData);

    @DeleteExchange("/{name}")
    ApiResponse<Boolean> deleteEmployeeByName(@PathVariable String name, @RequestBody Map<String, Object> requestBody);
}
