package com.reliaquest.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for external API communication (mock server)
 * Uses simple field names without employee_ prefix
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalEmployeeDTO {
    private String name;
    private Integer salary;
    private Integer age;
    private String title;
    private String email;

    /**
     * Creates ExternalEmployeeDTO from EmployeeCreateRequestDTO
     */
    public static ExternalEmployeeDTO fromCreateRequest(EmployeeCreateRequestDTO request) {
        return ExternalEmployeeDTO.builder()
                .name(request.getEmployeeName())
                .salary(request.getEmployeeSalary())
                .age(request.getEmployeeAge())
                .title(request.getEmployeeTitle())
                .email(request.getEmployeeEmail())
                .build();
    }

    /**
     * Converts to EmployeeDTO for internal use
     */
    public EmployeeDTO toEmployeeDTO() {
        return EmployeeDTO.builder()
                .name(this.name)
                .salary(this.salary)
                .age(this.age)
                .title(this.title)
                .email(this.email)
                .build();
    }
}
