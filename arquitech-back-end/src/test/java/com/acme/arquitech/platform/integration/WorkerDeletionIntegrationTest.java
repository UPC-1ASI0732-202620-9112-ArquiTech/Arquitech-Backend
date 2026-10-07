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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WorkerDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String supervisorToken;
    private long projectId;

    @BeforeEach
    void setUp() throws Exception {
        // A supervisor, a contractor and a project
        String supervisorEmail = "supervisor" + System.nanoTime() + "@test.com";
        String contractorEmail = "contractor" + System.nanoTime() + "@test.com";
        long supervisorId = signUp(supervisorEmail, "SUPERVISOR");
        long contractorId = signUp(contractorEmail, "CONTRACTOR");
        supervisorToken = signIn(supervisorEmail);
        projectId = createProject(supervisorId, contractorId);
    }

    @Test
    @DisplayName("Deleting a worker with assigned tasks returns 409 WORKER_HAS_TASKS and the worker still exists")
    void workerWithTasksCannotBeDeleted() throws Exception {
        long workerId = createWorker("Pedro Huaman");
        createTask(workerId);

        mockMvc.perform(delete("/api/v1/workers/{id}", workerId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKER_HAS_TASKS"));

        mockMvc.perform(get("/api/v1/workers/{id}", workerId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deleting a worker without tasks returns 204 and the worker no longer exists")
    void workerWithoutTasksCanBeDeleted() throws Exception {
        long workerId = createWorker("Ana Lopez");

        mockMvc.perform(delete("/api/v1/workers/{id}", workerId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/workers/{id}", workerId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isNotFound());
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

    private long createWorker(String fullName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/workers")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId": %d, "fullName": "%s", "role": "Builder", "specialty": "Concrete",
                                 "hireDate": "2026-01-10", "status": "ACTIVE"}
                                """.formatted(projectId, fullName)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long createTask(long workerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId": %d, "workerId": %d, "title": "Armado de columnas",
                                 "description": "Columnas del piso 8", "status": "PENDING", "dueDate": "2026-12-01"}
                                """.formatted(projectId, workerId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long readId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}