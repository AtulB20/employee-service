package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for creating new employees
 */
@Data
@Builder
public class EmployeeCreateRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String employeeName;

    @NotNull(message = "Salary is required") @Min(value = 1, message = "Salary must be greater than zero")
    private Integer employeeSalary;

    @NotNull(message = "Age is required") @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer employeeAge;

    @NotBlank(message = "Title cannot be blank")
    private String employeeTitle;

    private String employeeEmail;
}
