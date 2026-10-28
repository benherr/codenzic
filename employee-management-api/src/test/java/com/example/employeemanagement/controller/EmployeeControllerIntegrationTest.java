package com.example.employeemanagement.controller;

import com.example.employeemanagement.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests over the real HTTP stack, H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Map<String, Object> validPayload;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        validPayload = Map.of(
                "firstName", "Grace",
                "lastName", "Hopper",
                "email", "grace@example.com",
                "department", "Engineering",
                "position", "Backend Developer",
                "salary", 62000.00
        );
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("POST /api/v1/employees returns 201 with Location header")
    void create_returns201AndLocation() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("grace@example.com"));
    }

    @Test
    @DisplayName("POST with invalid body returns 400 and per-field errors")
    void create_withInvalidBody_returns400() throws Exception {
        String bad = json(Map.of(
                "firstName", "",
                "lastName", "X",
                "email", "not-an-email",
                "department", "",
                "position", "",
                "salary", -5
        ));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists());
    }

    @Test
    @DisplayName("POST duplicate email returns 409")
    void create_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} returns the employee")
    void findById_returnsEmployee() throws Exception {
        String created = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Grace Hopper"));
    }

    @Test
    @DisplayName("GET unknown id returns 404 with error payload")
    void findById_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/employees/9999"));
    }

    @Test
    @DisplayName("GET list supports pagination and department filter")
    void findAll_supportsPaginationAndFilter() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/employees")
                        .param("department", "Engineering")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/employees").param("department", "Finance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("PUT updates the employee")
    void update_returnsUpdatedEmployee() throws Exception {
        String created = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        Map<String, Object> update = Map.of(
                "firstName", "Grace",
                "lastName", "Hopper",
                "email", "grace@example.com",
                "department", "Research",
                "position", "Principal Engineer",
                "salary", 95000.00
        );

        mockMvc.perform(put("/api/v1/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Research"))
                .andExpect(jsonPath("$.position").value("Principal Engineer"));
    }

    @Test
    @DisplayName("PUT unknown id returns 404")
    void update_unknown_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/employees/{id}", 123456)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE returns 204 then 404 on second call")
    void delete_returns204ThenNotFound() throws Exception {
        String created = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayload)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isNotFound());
    }
}
