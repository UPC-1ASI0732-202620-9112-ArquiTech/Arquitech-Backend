package com.acme.arquitech.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {
    private static final String API = "/api/v1";
    private static final String PASSWORD = "SecurePass123!";
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void authenticationAndOpenApiContracts() throws Exception {
        String email = email("auth");
        long id = signUp("Auth Supervisor", email, "SUPERVISOR");

        mvc.perform(post(API + "/authentication/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Duplicate", email, "SUPERVISOR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

        mvc.perform(post(API + "/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        MvcResult signIn = mvc.perform(post(API + "/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.fullName").value("Auth Supervisor"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        assertThat(body(signIn).get("token").asText()).isNotBlank();

        mvc.perform(get(API + "/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        MvcResult docs = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andReturn();
        JsonNode paths = body(docs).get("paths");
        int operations = 0;
        for (JsonNode path : paths) {
            for (String method : new String[]{"get", "post", "put", "delete", "patch"}) {
                if (path.has(method)) operations++;
            }
        }
        assertThat(operations).isEqualTo(43);

        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void projectScopingAndRoleAuthorization() throws Exception {
        Account supervisor = account("scope-supervisor", "SUPERVISOR");
        Account contractor = account("scope-contractor", "CONTRACTOR");
        Account otherSupervisor = account("other-supervisor", "SUPERVISOR");
        Account otherContractor = account("other-contractor", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Scoped project");
        long foreignProjectId = createProject(otherSupervisor, otherContractor.id(), "Foreign project");

        mvc.perform(get(API + "/projects/supervisor/{id}", supervisor.id()).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(projectId));
        mvc.perform(get(API + "/projects/contractor/{id}", contractor.id()).header("Authorization", bearer(contractor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(projectId));
        mvc.perform(get(API + "/projects").header("Authorization", bearer(contractor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(projectId));
        mvc.perform(get(API + "/projects/{id}", foreignProjectId).header("Authorization", bearer(contractor)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));

        String resource = materialJson(projectId, "Restricted", "1.0");
        mvc.perform(post(API + "/materials").header("Authorization", bearer(contractor))
                        .contentType(MediaType.APPLICATION_JSON).content(resource))
                .andExpect(status().isForbidden());
        mvc.perform(put(API + "/materials/1").header("Authorization", bearer(contractor))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete(API + "/materials/1").header("Authorization", bearer(contractor)))
                .andExpect(status().isForbidden());

        mvc.perform(post(API + "/projects").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson(supervisor.id(), 999999L, "Invalid contractor")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_CONTRACTOR"));
    }

    @Test
    void decimalMaterialsAreTransactionalAndMachinerySerialIsGlobal() throws Exception {
        Account supervisor = account("inventory-supervisor", "SUPERVISOR");
        Account contractor = account("inventory-contractor", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Inventory A");
        long secondProjectId = createProject(supervisor, contractor.id(), "Inventory B");

        MvcResult created = mvc.perform(post(API + "/materials").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(materialJson(projectId, "Cement", "1.25")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(1.25))
                .andExpect(jsonPath("$.stock").value(1.25)).andReturn();
        long materialId = body(created).get("id").asLong();

        mvc.perform(post(API + "/materials/{id}/entry", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":0.5,"supplier":"Supplier","occurredAt":"2026-10-02T16:30:00Z","note":"delivery"}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(0.5));
        mvc.perform(post(API + "/materials/{id}/use", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":0.25,"occurredAt":"2026-10-02T18:00:00Z","note":"usage"}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(0.25));
        mvc.perform(post(API + "/materials/{id}/use", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":99.125,"occurredAt":"2026-10-02T19:00:00Z"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        mvc.perform(get(API + "/materials/{id}", materialId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(1.75))
                .andExpect(jsonPath("$.stock").value(1.5));
        mvc.perform(get(API + "/materials/project/{id}/history", projectId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));

        String machine = machineryJson(projectId, "GLOBAL-001", "Excavator");
        MvcResult machineCreated = mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(machine))
                .andExpect(status().isCreated()).andReturn();
        long machineId = body(machineCreated).get("id").asLong();
        mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(secondProjectId, "GLOBAL-001", "Crane")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATED_SERIAL_NUMBER"));
        mvc.perform(put(API + "/machinery/{id}", machineId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, "GLOBAL-001", "Updated excavator")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated excavator"));
        mvc.perform(delete(API + "/machinery/{id}", machineId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());

        mvc.perform(delete(API + "/materials/{id}", materialId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(get(API + "/materials/{id}", materialId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNotFound());
    }

    @Test
    void workforceIncidentsAndProfileEnforceDomainRules() throws Exception {
        Account supervisor = account("work-supervisor", "SUPERVISOR");
        Account contractor = account("work-contractor", "CONTRACTOR");
        Account other = account("profile-other", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Work project");
        long secondProjectId = createProject(supervisor, contractor.id(), "Other work project");

        long workerId = createWorker(supervisor, projectId, "Builder One");
        long foreignWorkerId = createWorker(supervisor, secondProjectId, "Builder Two");
        mvc.perform(post(API + "/tasks").header("Authorization", bearer(supervisor)).contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson(projectId, foreignWorkerId, "PENDING")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        MvcResult task = mvc.perform(post(API + "/tasks").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(taskJson(projectId, workerId, "PENDING")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.completedAt").doesNotExist()).andReturn();
        long taskId = body(task).get("id").asLong();
        mvc.perform(put(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(taskJson(projectId, workerId, "COMPLETED")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").isNotEmpty());
        mvc.perform(put(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(taskJson(projectId, workerId, "IN_PROGRESS")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").doesNotExist());
        mvc.perform(delete(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("WORKER_HAS_TASKS"));
        mvc.perform(delete(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());

        MvcResult incident = mvc.perform(post(API + "/incidents").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson(projectId, "OPEN", other.id())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.reportedByUserId").value(supervisor.id()))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist()).andReturn();
        long incidentId = body(incident).get("id").asLong();
        mvc.perform(put(API + "/incidents/{id}", incidentId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON).content(incidentJson(projectId, "RESOLVED", other.id())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resolvedAt").isNotEmpty())
                .andExpect(jsonPath("$.reportedByUserId").value(supervisor.id()));
        mvc.perform(delete(API + "/incidents/{id}", incidentId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());

        mvc.perform(put(API + "/users/{id}", supervisor.id()).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Updated Supervisor","phone":"+51 999 888 777"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Updated Supervisor"));
        mvc.perform(put(API + "/users/{id}", other.id()).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Forbidden","phone":"123"}
                                """))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private Account account(String prefix, String role) throws Exception {
        String email = email(prefix);
        long id = signUp(prefix, email, role);
        MvcResult result = mvc.perform(post(API + "/authentication/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk()).andReturn();
        return new Account(id, body(result).get("token").asText());
    }

    private long signUp(String name, String email, String role) throws Exception {
        MvcResult result = mvc.perform(post(API + "/authentication/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, email, role)))
                .andExpect(status().isCreated()).andReturn();
        return body(result).get("id").asLong();
    }

    private long createProject(Account supervisor, long contractorId, String name) throws Exception {
        MvcResult result = mvc.perform(post(API + "/projects").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson(supervisor.id(), contractorId, name)))
                .andExpect(status().isCreated()).andReturn();
        return body(result).get("id").asLong();
    }

    private long createWorker(Account supervisor, long projectId, String name) throws Exception {
        MvcResult result = mvc.perform(post(API + "/workers").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectId":%d,"fullName":"%s","role":"Builder","specialty":"Concrete","hireDate":"2026-01-10","status":"ACTIVE"}
                                """.formatted(projectId, name)))
                .andExpect(status().isCreated()).andReturn();
        return body(result).get("id").asLong();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsByteArray());
    }

    private String email(String prefix) { return prefix + SEQUENCE.incrementAndGet() + "@arquitech.test"; }
    private String bearer(Account account) { return "Bearer " + account.token(); }
    private String userJson(String name, String email, String role) {
        return """
                {"fullName":"%s","email":"%s","password":"%s","role":"%s","phone":"999999999"}
                """
                .formatted(name, email, PASSWORD, role);
    }
    private String projectJson(long supervisorId, long contractorId, String name) {
        return """
                {"name":"%s","location":"Lima","startDate":"2026-01-01","endDate":"2026-12-31","budget":1000.50,"status":"ACTIVE","progress":10,"supervisorId":%d,"contractorId":%d}
                """
                .formatted(name, supervisorId, contractorId);
    }
    private String materialJson(long projectId, String name, String quantity) {
        return """
                {"projectId":%d,"name":"%s","unit":"kg","quantity":%s,"minimumStock":0.5,"unitPrice":12.50,"provider":"Supplier","providerRuc":"20123456789","date":"2026-10-02"}
                """
                .formatted(projectId, name, quantity);
    }
    private String machineryJson(long projectId, String serial, String name) {
        return """
                {"projectId":%d,"name":"%s","serialNumber":"%s","registeredAt":"2026-01-02","status":"OPERATIONAL","description":"Machine"}
                """
                .formatted(projectId, name, serial);
    }
    private String taskJson(long projectId, long workerId, String status) {
        return """
                {"projectId":%d,"workerId":%d,"title":"Foundation","description":"Build foundation","status":"%s","dueDate":"2026-12-01","completedAt":"2000-01-01T00:00:00Z"}
                """
                .formatted(projectId, workerId, status);
    }
    private String incidentJson(long projectId, String status, long reportedBy) {
        return """
                {"projectId":%d,"type":"Safety","description":"Safety incident","severity":"HIGH","status":"%s","reportedAt":"2026-10-02T10:00:00Z","reportedByUserId":%d,"resolvedAt":"2000-01-01T00:00:00Z"}
                """
                .formatted(projectId, status, reportedBy);
    }

    private record Account(long id, String token) {}
}
