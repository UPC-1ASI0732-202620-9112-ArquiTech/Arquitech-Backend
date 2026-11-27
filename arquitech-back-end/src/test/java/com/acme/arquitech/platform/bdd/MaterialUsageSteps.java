package com.acme.arquitech.platform.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MaterialUsageSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String supervisorToken;
    private long projectId;
    private long materialId;
    private ResultActions lastResponse;
    private final java.util.List<Long> workerIds = new java.util.ArrayList<>();

    @Given("a supervisor has a project with a material that has a stock of {int}")
    public void aSupervisorHasAProjectWithAMaterial(int stock) throws Exception {
        String supervisorEmail = "bdd.supervisor" + System.nanoTime() + "@test.com";
        String contractorEmail = "bdd.contractor" + System.nanoTime() + "@test.com";
        long supervisorId = signUp(supervisorEmail, "SUPERVISOR");
        long contractorId = signUp(contractorEmail, "CONTRACTOR");
        supervisorToken = signIn(supervisorEmail);
        projectId = createProject(supervisorId, contractorId);
        materialId = createMaterial(projectId, stock);
    }

    @When("the supervisor registers a usage of {int} units")
    public void theSupervisorRegistersAUsage(int quantity) throws Exception {
        lastResponse = mockMvc.perform(post("/api/v1/materials/{id}/use", materialId)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": %d, "occurredAt": "2026-10-07T10:00:00Z", "note": "BDD usage"}
                        """.formatted(quantity)));
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int statusCode) throws Exception {
        lastResponse.andExpect(status().is(statusCode));
    }

    @Then("the response status is {int} with error code {string}")
    public void theResponseStatusIsWithErrorCode(int statusCode, String code) throws Exception {
        lastResponse.andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.code").value(code));
    }

    @Then("the material stock is {int}")
    public void theMaterialStockIs(int expectedStock) throws Exception {
        mockMvc.perform(get("/api/v1/materials/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value((double) expectedStock));
    }

    @When("the supervisor registers an entry of {int} units without supplier")
    public void theSupervisorRegistersAnEntryWithoutSupplier(int quantity) throws Exception {
        lastResponse = mockMvc.perform(post("/api/v1/materials/{id}/entry", materialId)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": %d, "occurredAt": "2026-10-07T10:00:00Z"}
                        """.formatted(quantity)));
    }

    @When("the supervisor syncs the offline usage {string} of {int} units")
    public void theSupervisorSyncsTheOfflineUsage(String clientRequestId, int quantity) throws Exception {
        lastResponse = mockMvc.perform(post("/api/v1/materials/{id}/offline-movements", materialId)
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clientRequestId": "%s", "type": "USAGE", "quantity": %d,
                         "occurredAt": "2026-10-07T10:00:00Z", "note": "Offline draft"}
                        """.formatted(clientRequestId, quantity)));
    }

    @Then("the project history has {int} usage movements")
    public void theProjectHistoryHasUsageMovements(int expected) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/materials/project/{projectId}/history", projectId)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andReturn();
        long usages = 0;
        for (var movement : objectMapper.readTree(result.getResponse().getContentAsString())) {
            if ("USAGE".equals(movement.get("type").asText())) usages++;
        }
        org.junit.jupiter.api.Assertions.assertEquals(expected, usages);
    }

    @Given("the project has {int} active workers")
    public void theProjectHasActiveWorkers(int count) throws Exception {
        workerIds.clear();
        for (int index = 1; index <= count; index++) {
            MvcResult result = mockMvc.perform(post("/api/v1/workers")
                            .header("Authorization", "Bearer " + supervisorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"projectId": %d, "fullName": "Trabajador %d", "role": "Operario",
                                     "hireDate": "2026-01-15", "status": "ACTIVE"}
                                    """.formatted(projectId, index)))
                    .andExpect(status().isCreated())
                    .andReturn();
            workerIds.add(readId(result));
        }
    }

    @When("the supervisor records every worker as {string} on {string}")
    public void theSupervisorRecordsEveryWorker(String attendanceStatus, String date) throws Exception {
        for (Long workerId : workerIds) {
            recordAttendance(workerId, attendanceStatus, date).andExpect(status().isCreated());
        }
    }

    @When("the supervisor records the first worker as {string} on {string}")
    public void theSupervisorRecordsTheFirstWorker(String attendanceStatus, String date) throws Exception {
        lastResponse = recordAttendance(workerIds.get(0), attendanceStatus, date);
    }

    @Then("{int} attendance records exist for {string}")
    public void attendanceRecordsExist(int expected, String date) throws Exception {
        mockMvc.perform(get("/api/v1/attendance")
                        .param("projectId", String.valueOf(projectId))
                        .param("date", date)
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expected));
    }

    private ResultActions recordAttendance(long workerId, String attendanceStatus, String date) throws Exception {
        return mockMvc.perform(post("/api/v1/attendance")
                .header("Authorization", "Bearer " + supervisorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"projectId": %d, "workerId": %d, "attendanceDate": "%s", "status": "%s"}
                        """.formatted(projectId, workerId, date, attendanceStatus)));
    }

    private long signUp(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "BDD User", "email": "%s", "password": "SecurePass123!", "role": "%s", "phone": "999999999"}
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