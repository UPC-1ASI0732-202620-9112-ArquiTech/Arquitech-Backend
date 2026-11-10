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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class MaterialStockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String supervisorToken;
    private long projectId;
    private long materialId;

    @BeforeEach
    void setUp() throws Exception {
        // A supervisor, a contractor, a project and a material with stock 40
        String supervisorEmail = "supervisor" + System.nanoTime() + "@test.com";
        String contractorEmail = "contractor" + System.nanoTime() + "@test.com";
        long supervisorId = signUp(supervisorEmail, "SUPERVISOR");
        long contractorId = signUp(contractorEmail, "CONTRACTOR");
        supervisorToken = signIn(supervisorEmail);
        projectId = createProject(supervisorId, contractorId);
        materialId = createMaterial(projectId, 40);
    }

    @Test
    @DisplayName("Usage within the stock returns 201 and the stock decreases from 40 to 30")
    void usageWithinStockDecreasesStock() throws Exception {
        mockMvc.perform(post("/api/v1/materials/{id}/use", materialId)
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 10, "occurredAt": "2026-10-03T10:00:00Z", "note": "Columns floor 8"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/materials/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(30.0));
    }

    @Test
    @DisplayName("Usage above the stock returns 400 INSUFFICIENT_STOCK and the stock stays at 40")
    void usageAboveStockIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/materials/{id}/use", materialId)
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 50, "occurredAt": "2026-10-03T10:00:00Z", "note": "Too much"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        mockMvc.perform(get("/api/v1/materials/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(40.0));
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

    private long createMaterial(long projectId, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/materials")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId": %d, "name": "Cemento", "unit": "bolsa", "quantity": %d, "minimumStock": 5,
                                 "unitPrice": 25.60, "provider": "Constructora Lima", "providerRuc": "20100124567", "date": "2026-10-01"}
                                """.formatted(projectId, quantity)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}