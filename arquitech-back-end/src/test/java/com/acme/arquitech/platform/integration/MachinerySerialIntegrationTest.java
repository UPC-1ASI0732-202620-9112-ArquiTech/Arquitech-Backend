package com.acme.arquitech.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class MachinerySerialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String supervisorToken;
    private long projectA;
    private long projectB;

    @BeforeEach
    void setUp() throws Exception {
        // A supervisor, a contractor and two projects
        String supervisorEmail = "supervisor" + System.nanoTime() + "@test.com";
        String contractorEmail = "contractor" + System.nanoTime() + "@test.com";
        long supervisorId = signUp(supervisorEmail, "SUPERVISOR");
        long contractorId = signUp(contractorEmail, "CONTRACTOR");
        supervisorToken = signIn(supervisorEmail);
        projectA = createProject(supervisorId, contractorId);
        projectB = createProject(supervisorId, contractorId);
    }

    @Test
    @DisplayName("Registering a repeated serial number in the same project returns 409 DUPLICATED_SERIAL_NUMBER")
    void repeatedSerialInSameProjectIsRejected() throws Exception {
        registerMachinery(projectA, "EXC-001", "Excavadora")
                .andExpect(status().isCreated());

        registerMachinery(projectA, "EXC-001", "Otra excavadora")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATED_SERIAL_NUMBER"));
    }

    @Test
    @DisplayName("Registering the same serial number in a different project returns 201")
    void sameSerialInDifferentProjectIsAllowed() throws Exception {
        registerMachinery(projectA, "EXC-001", "Excavadora")
                .andExpect(status().isCreated());

        registerMachinery(projectB, "EXC-001", "Excavadora de la obra B")
                .andExpect(status().isCreated());
    }

    // ---------- Helpers to prepare the data through the API ----------

    private long signUp(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Test User", "email": "%s", "password": "SecurePass123!", "role": "%s", "phone": "999999999"}
                                """.formatted(email, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private String signIn(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "SecurePass123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createProject(long supervisorId, long contractorId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Torre Norte", "location": "Lima", "startDate": "2026-01-01", "endDate": "2026-12-31",
                                 "budget": 1000.50, "status": "ACTIVE", "progress": 10, "supervisorId": %d, "contractorId": %d}
                                """.formatted(supervisorId, contractorId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private ResultActions registerMachinery(long projectId, String serialNumber, String name) throws Exception {
        return mockMvc.perform(post("/api/v1/machinery")
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"projectId": %d, "name": "%s", "serialNumber": "%s", "registeredAt": "2026-01-02",
                         "status": "OPERATIONAL", "description": "Machine"}
                        """.formatted(projectId, name, serialNumber)));
    }

    private long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}