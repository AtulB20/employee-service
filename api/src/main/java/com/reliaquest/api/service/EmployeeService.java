package com.reliaquest.api.service;

import com.reliaquest.api.model.EmployeeCreateRequest;
import com.reliaquest.api.model.EmployeeDTO;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    List<EmployeeDTO> getAllEmployees();

    List<EmployeeDTO> getEmployeesByNameSearch(String searchString);

    EmployeeDTO getEmployeeById(UUID id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    EmployeeDTO createEmployee(EmployeeCreateRequest employeeInput);

    String deleteEmployeeById(UUID id);
}
