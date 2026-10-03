package com.acme.arquitech.platform;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.SignInResource;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.SignUpResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {
    private static final String API = "/api/v1";
    private static final String PASSWORD = "SecurePass123!";
    private static final String TEST_JWT_SECRET = "ArquiTechIntegrationTestSecretAtLeastThirtyTwoBytesLong";
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void authenticationUsersAndOpenApiExposeTheFinalContract() throws Exception {
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
        String token = body(signIn).get("token").asText();
        assertThat(token).isNotBlank();
        assertThat(new SignInResource(email, PASSWORD).toString()).doesNotContain(PASSWORD).contains("[REDACTED]");
        assertThat(new SignUpResource("Auth Supervisor", email, PASSWORD, Role.SUPERVISOR, null, null).toString())
                .doesNotContain(PASSWORD).contains("[REDACTED]");
        assertThat(new AuthenticatedUserResource(id, "Auth Supervisor", email, Role.SUPERVISOR, token).toString())
                .doesNotContain(token).contains("[REDACTED]");

        mvc.perform(get(API + "/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get(API + "/projects").header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        String expiredToken = Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(yesterday.minus(1, ChronoUnit.DAYS)))
                .expiration(Date.from(yesterday))
                .signWith(Keys.hmacShaKeyFor(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        mvc.perform(get(API + "/projects").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get(API + "/users/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        mvc.perform(get(API + "/users/{id}", 999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        MvcResult docs = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andReturn();
        JsonNode paths = body(docs).get("paths");
        Set<String> actualOperations = new TreeSet<>();
        var pathNames = paths.fieldNames();
        while (pathNames.hasNext()) {
            String pathName = pathNames.next();
            JsonNode path = paths.get(pathName);
            for (String method : new String[]{"get", "post", "put", "delete", "patch"}) {
                if (path.has(method)) actualOperations.add(method.toUpperCase() + " " + pathName);
            }
        }
        assertThat(actualOperations).containsExactlyInAnyOrderElementsOf(Set.of(
                "POST " + API + "/authentication/sign-in",
                "POST " + API + "/authentication/sign-up",
                "GET " + API + "/users",
                "GET " + API + "/users/{id}",
                "GET " + API + "/projects",
                "GET " + API + "/projects/supervisor/{userId}",
                "POST " + API + "/projects",
                "GET " + API + "/materials/project/{projectId}",
                "POST " + API + "/materials",
                "PUT " + API + "/materials/{id}",
                "DELETE " + API + "/materials/{id}",
                "POST " + API + "/materials/{id}/entry",
                "POST " + API + "/materials/{id}/use",
                "GET " + API + "/materials/project/{projectId}/history",
                "GET " + API + "/machinery",
                "GET " + API + "/machinery/{id}",
                "POST " + API + "/machinery",
                "PUT " + API + "/machinery/{id}",
                "DELETE " + API + "/machinery/{id}",
                "GET " + API + "/workers",
                "GET " + API + "/workers/{id}",
                "POST " + API + "/workers",
                "PUT " + API + "/workers/{id}",
                "DELETE " + API + "/workers/{id}",
                "GET " + API + "/tasks",
                "POST " + API + "/tasks",
                "PUT " + API + "/tasks/{id}",
                "DELETE " + API + "/tasks/{id}",
                "GET " + API + "/incidents/project/{projectId}",
                "POST " + API + "/incidents",
                "PUT " + API + "/incidents/{id}",
                "DELETE " + API + "/incidents/{id}"
        ));
        assertThat(actualOperations).hasSize(32);

        assertMissing(paths, API + "/projects/{id}", "get");
        assertMissing(paths, API + "/projects/contractor/{userId}", "get");
        assertMissing(paths, API + "/materials", "get");
        assertMissing(paths, API + "/materials/{id}", "get");
        assertMissing(paths, API + "/materials/project/{projectId}/history/{materialName}", "get");
        assertMissing(paths, API + "/materials/{id}/low-inventory", "get");
        assertMissing(paths, API + "/users/{id}", "put");
        assertMissing(paths, API + "/tasks/{id}", "get");
        assertMissing(paths, API + "/incidents", "get");
        assertMissing(paths, API + "/incidents/{id}", "get");
        assertMissing(paths, API + "/incidents/{id}/report", "get");

        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void projectScopingAndRoleAuthorizationAreServerSide() throws Exception {
        Account supervisor = account("scope-supervisor", "SUPERVISOR");
        Account contractor = account("scope-contractor", "CONTRACTOR");
        Account otherSupervisor = account("other-supervisor", "SUPERVISOR");
        Account otherContractor = account("other-contractor", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Scoped project");
        long foreignProjectId = createProject(otherSupervisor, otherContractor.id(), "Foreign project");

        mvc.perform(get(API + "/users/{id}", contractor.id())
                        .header("Authorization", bearer(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractor.id()));
        mvc.perform(get(API + "/users/{id}", otherContractor.id())
                        .header("Authorization", bearer(contractor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get(API + "/users/{id}", otherContractor.id())
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otherContractor.id()));

        mvc.perform(get(API + "/projects/supervisor/{id}", supervisor.id())
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(projectId));
        mvc.perform(get(API + "/projects").header("Authorization", bearer(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(projectId));
        mvc.perform(get(API + "/materials/project/{id}", projectId)
                        .header("Authorization", bearer(contractor)))
                .andExpect(status().isOk());
        mvc.perform(get(API + "/materials/project/{id}", foreignProjectId)
                        .header("Authorization", bearer(contractor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mvc.perform(post(API + "/materials").header("Authorization", bearer(contractor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialJson(projectId, "Restricted", "1.0", "2026-10-02T17:00:00.000Z")))
                .andExpect(status().isForbidden());
        mvc.perform(put(API + "/materials/1").header("Authorization", bearer(contractor))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete(API + "/materials/1").header("Authorization", bearer(contractor)))
                .andExpect(status().isForbidden());

        mvc.perform(post(API + "/projects").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson(supervisor.id(), 999999L, "Invalid contractor")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CONTRACTOR"));
    }

    @Test
    void decimalMaterialsAndProjectScopedMachineryAreConsistent() throws Exception {
        Account supervisor = account("inventory-supervisor", "SUPERVISOR");
        Account contractor = account("inventory-contractor", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Inventory A");
        long secondProjectId = createProject(supervisor, contractor.id(), "Inventory B");

        MvcResult created = mvc.perform(post(API + "/materials").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialJson(projectId, "Cement", "1.25", "2026-10-02T17:00:00.000Z")))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.quantity").value(1.25))
                .andExpect(jsonPath("$.stock").value(1.25))
                .andExpect(jsonPath("$.date").value("2026-10-02"))
                .andReturn();
        long materialId = body(created).get("id").asLong();

        MvcResult dateOnlyMaterial = mvc.perform(post(API + "/materials")
                        .header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(materialJson(secondProjectId, "Sand", "0.01", "2026-10-02")))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.date").value("2026-10-02"))
                .andReturn();
        long dateOnlyMaterialId = body(dateOnlyMaterial).get("id").asLong();

        mvc.perform(post(API + "/materials/{id}/entry", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":0.5,"supplier":"Supplier","occurredAt":"2026-10-02T16:30:00Z","note":"delivery"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0.5))
                .andExpect(jsonPath("$.occurredAt").isNotEmpty());
        mvc.perform(post(API + "/materials/{id}/use", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":0.25,"occurredAt":"2026-10-02T18:00:00Z","note":"usage"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0.25));
        mvc.perform(post(API + "/materials/{id}/use", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":99.125,"occurredAt":"2026-10-02T19:00:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        mvc.perform(put(API + "/materials/{id}", materialId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cement updated","unit":"kg","minimumStock":0.75,"unitPrice":13.25,
                                 "provider":"Supplier updated","providerRuc":"20123456789"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cement updated"));
        mvc.perform(get(API + "/materials/project/{id}", projectId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(1.75))
                .andExpect(jsonPath("$[0].stock").value(1.5));
        mvc.perform(get(API + "/materials/project/{id}/history", projectId)
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        String serial = "PROJECT-001";
        MvcResult firstMachine = mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, serial, "Excavator")))
                .andExpect(status().isCreated()).andReturn();
        long firstMachineId = body(firstMachine).get("id").asLong();
        String firstMachineLocation = firstMachine.getResponse().getHeader("Location");
        assertThat(firstMachineLocation).isEqualTo(API + "/machinery/" + firstMachineId);
        mvc.perform(get(firstMachineLocation).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstMachineId));
        mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, serial, "Duplicate excavator")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATED_SERIAL_NUMBER"));
        MvcResult secondMachine = mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(secondProjectId, serial, "Crane")))
                .andExpect(status().isCreated()).andReturn();
        long secondMachineId = body(secondMachine).get("id").asLong();
        assertThat(secondMachine.getResponse().getHeader("Location"))
                .isEqualTo(API + "/machinery/" + secondMachineId);

        MvcResult thirdMachine = mvc.perform(post(API + "/machinery").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, "PROJECT-002", "Loader")))
                .andExpect(status().isCreated()).andReturn();
        long thirdMachineId = body(thirdMachine).get("id").asLong();
        mvc.perform(put(API + "/machinery/{id}", thirdMachineId)
                        .header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, serial, "Conflicting loader")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATED_SERIAL_NUMBER"));

        mvc.perform(get(API + "/machinery/{id}", firstMachineId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.projectId").value(projectId));
        mvc.perform(put(API + "/machinery/{id}", firstMachineId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(machineryJson(projectId, serial, "Updated excavator")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated excavator"));
        mvc.perform(delete(API + "/machinery/{id}", firstMachineId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(API + "/machinery/{id}", secondMachineId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(API + "/machinery/{id}", thirdMachineId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());

        mvc.perform(delete(API + "/materials/{id}", materialId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(API + "/materials/{id}", dateOnlyMaterialId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(get(API + "/materials/project/{id}", projectId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void workforceTasksAndIncidentsEnforceDomainRules() throws Exception {
        Account supervisor = account("work-supervisor", "SUPERVISOR");
        Account contractor = account("work-contractor", "CONTRACTOR");
        long projectId = createProject(supervisor, contractor.id(), "Work project");
        long secondProjectId = createProject(supervisor, contractor.id(), "Other work project");

        long workerId = createWorker(supervisor, projectId, "Builder One");
        long foreignWorkerId = createWorker(supervisor, secondProjectId, "Builder Two");
        mvc.perform(get(API + "/workers").param("projectId", String.valueOf(projectId))
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(workerId));
        mvc.perform(put(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workerJson(projectId, "Builder Updated", "ON_LEAVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Builder Updated"))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        mvc.perform(post(API + "/tasks").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson(projectId, foreignWorkerId, "PENDING")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        MvcResult task = mvc.perform(post(API + "/tasks").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson(projectId, workerId, "PENDING")))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.completedAt").doesNotExist()).andReturn();
        long taskId = body(task).get("id").asLong();
        mvc.perform(get(API + "/tasks").param("projectId", String.valueOf(projectId))
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson(projectId, workerId, "COMPLETED")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").isNotEmpty());
        mvc.perform(put(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson(projectId, workerId, "IN_PROGRESS")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").doesNotExist());
        mvc.perform(get(API + "/tasks").param("projectId", String.valueOf(projectId))
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].completedAt").doesNotExist());
        mvc.perform(delete(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORKER_HAS_TASKS"));
        mvc.perform(delete(API + "/tasks/{id}", taskId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(API + "/workers/{id}", workerId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());

        MvcResult incident = mvc.perform(post(API + "/incidents").header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson(projectId, "OPEN", contractor.id())))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.reportedByUserId").value(supervisor.id()))
                .andExpect(jsonPath("$.resolvedAt").doesNotExist()).andReturn();
        long incidentId = body(incident).get("id").asLong();
        mvc.perform(get(API + "/incidents/project/{id}", projectId)
                        .header("Authorization", bearer(contractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put(API + "/incidents/{id}", incidentId).header("Authorization", bearer(supervisor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incidentJson(projectId, "RESOLVED", contractor.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty())
                .andExpect(jsonPath("$.reportedByUserId").value(supervisor.id()));
        mvc.perform(delete(API + "/incidents/{id}", incidentId).header("Authorization", bearer(supervisor)))
                .andExpect(status().isNoContent());
        mvc.perform(get(API + "/incidents/project/{id}", projectId)
                        .header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    private void assertMissing(JsonNode paths, String path, String method) {
        assertThat(paths.has(path) && paths.get(path).has(method))
                .as("%s %s must not be public", method.toUpperCase(), path)
                .isFalse();
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
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andReturn();
        return body(result).get("id").asLong();
    }

    private long createWorker(Account supervisor, long projectId, String name) throws Exception {
        MvcResult result = mvc.perform(post(API + "/workers").header("Authorization", bearer(supervisor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(workerJson(projectId, name, "ACTIVE")))
                .andExpect(status().isCreated()).andReturn();
        long id = body(result).get("id").asLong();
        String location = result.getResponse().getHeader("Location");
        assertThat(location).isEqualTo(API + "/workers/" + id);
        mvc.perform(get(location).header("Authorization", bearer(supervisor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        return id;
    }

    private JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsByteArray());
    }

    private String email(String prefix) { return prefix + SEQUENCE.incrementAndGet() + "@arquitech.test"; }
    private String bearer(Account account) { return "Bearer " + account.token(); }
    private String userJson(String name, String email, String role) {
        return """
                {"fullName":"%s","email":"%s","password":"%s","role":"%s","phone":"999999999"}
                """.formatted(name, email, PASSWORD, role);
    }
    private String projectJson(long supervisorId, long contractorId, String name) {
        return """
                {"name":"%s","location":"Lima","startDate":"2026-01-01","endDate":"2026-12-31","budget":1000.50,"status":"ACTIVE","progress":10,"supervisorId":%d,"contractorId":%d}
                """.formatted(name, supervisorId, contractorId);
    }
    private String materialJson(long projectId, String name, String quantity, String date) {
        return """
                {"projectId":%d,"name":"%s","unit":"kg","quantity":%s,"minimumStock":0.5,"unitPrice":12.50,"provider":"Supplier","providerRuc":"20123456789","date":"%s"}
                """.formatted(projectId, name, quantity, date);
    }
    private String machineryJson(long projectId, String serial, String name) {
        return """
                {"projectId":%d,"name":"%s","serialNumber":"%s","registeredAt":"2026-01-02","status":"OPERATIONAL","description":"Machine"}
                """.formatted(projectId, name, serial);
    }
    private String workerJson(long projectId, String name, String status) {
        return """
                {"projectId":%d,"fullName":"%s","role":"Builder","specialty":"Concrete","hireDate":"2026-01-10","status":"%s"}
                """.formatted(projectId, name, status);
    }
    private String taskJson(long projectId, long workerId, String status) {
        return """
                {"projectId":%d,"workerId":%d,"title":"Foundation","description":"Build foundation","status":"%s","dueDate":"2026-12-01","completedAt":"2000-01-01T00:00:00Z"}
                """.formatted(projectId, workerId, status);
    }
    private String incidentJson(long projectId, String status, long reportedBy) {
        return """
                {"projectId":%d,"type":"Safety","description":"Safety incident","severity":"HIGH","status":"%s","reportedAt":"2026-10-02T10:00:00Z","reportedByUserId":%d,"resolvedAt":"2000-01-01T00:00:00Z"}
                """.formatted(projectId, status, reportedBy);
    }

    private record Account(long id, String token) {}
}
