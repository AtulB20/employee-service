package com.reliaquest.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

/**
 * Test to verify JSON serialization/deserialization of EmployeeDTO
 */
@JsonTest
class EmployeeDTOTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeToCorrectJsonPropertyNames() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        EmployeeDTO employee = EmployeeDTO.builder()
                .id(id)
                .name("John Doe")
                .salary(50000)
                .age(30)
                .title("Developer")
                .email("john.doe@company.com")
                .build();

        // When
        String json = objectMapper.writeValueAsString(employee);

        // Then
        assertThat(json).contains("\"employee_name\":\"John Doe\"");
        assertThat(json).contains("\"employee_salary\":50000");
        assertThat(json).contains("\"employee_age\":30");
        assertThat(json).contains("\"employee_title\":\"Developer\"");
        assertThat(json).contains("\"employee_email\":\"john.doe@company.com\"");
        assertThat(json).contains("\"id\":\"" + id.toString() + "\"");
    }

    @Test
    void shouldDeserializeFromJsonPropertyNames() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        String json = String.format(
                """
                {
                    "id": "%s",
                    "employee_name": "Jane Smith",
                    "employee_salary": 60000,
                    "employee_age": 25,
                    "employee_title": "Senior Developer",
                    "employee_email": "jane.smith@company.com"
                }
                """,
                id.toString());

        // When
        EmployeeDTO employee = objectMapper.readValue(json, EmployeeDTO.class);

        // Then
        assertThat(employee.getId()).isEqualTo(id);
        assertThat(employee.getName()).isEqualTo("Jane Smith");
        assertThat(employee.getSalary()).isEqualTo(60000);
        assertThat(employee.getAge()).isEqualTo(25);
        assertThat(employee.getTitle()).isEqualTo("Senior Developer");
        assertThat(employee.getEmail()).isEqualTo("jane.smith@company.com");
    }
}
