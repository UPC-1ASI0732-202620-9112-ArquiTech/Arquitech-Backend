package com.acme.arquitech.platform.shared.infrastructure.documentation.openapi.configuration;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI arquiTechOpenAPI(@Value("${documentation.application.version}") String version) {
        return new OpenAPI().info(new Info().title("ArquiTech REST API").version(version)
                .description("Construction projects, inventory, workforce and incidents. Supervisors manage their projects; contractors read their assigned projects."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer responseDocumentation() {
        return api -> {
            ModelConverters.getInstance().read(ErrorResponse.class).forEach(api.getComponents()::addSchemas);
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                boolean authentication = path.startsWith("/api/v1/authentication/");
                if (authentication) operation.setSecurity(java.util.List.of());
                if (method == PathItem.HttpMethod.POST && !path.endsWith("/sign-in")) {
                    var inferredSuccess = operation.getResponses().remove("200");
                    // Preserve the resource schema inferred by springdoc.
                    var success = operation.getResponses().get("201");
                    if (success == null) {
                        success = inferredSuccess == null ? new ApiResponse().description("Resource created") : inferredSuccess;
                        success.setDescription("Resource created");
                        operation.getResponses().addApiResponse("201", success);
                    }
                }
                if (method == PathItem.HttpMethod.DELETE) {
                    operation.getResponses().remove("200");
                    operation.getResponses().addApiResponse("204", new ApiResponse().description("Deleted"));
                }
                operation.getResponses().addApiResponse("400", error("Invalid request or business rule"));
                operation.getResponses().addApiResponse("401", error("Authentication required or invalid credentials"));
                if (!authentication) {
                    operation.getResponses().addApiResponse("403", error("Role or project access denied"));
                    operation.getResponses().addApiResponse("404", error("Resource not found"));
                }
                if (method != PathItem.HttpMethod.GET)
                    operation.getResponses().addApiResponse("409", error("Duplicate or conflicting data"));
            }));
            // Legacy database values are not accepted as functional request states.
            enums(api, "ProjectStatus", "ACTIVE", "PENDING", "COMPLETED", "SUSPENDED");
            enums(api, "MachineryStatus", "OPERATIONAL", "MAINTENANCE", "OUT_OF_SERVICE");
            enums(api, "TaskStatus", "PENDING", "IN_PROGRESS", "COMPLETED");
            enums(api, "IncidentStatus", "OPEN", "IN_REVIEW", "RESOLVED");
            enums(api, "Role", "SUPERVISOR", "CONTRACTOR");
        };
    }

    private ApiResponse error(String description) {
        return new ApiResponse().description(description).content(new Content().addMediaType("application/json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void enums(OpenAPI api, String name, String... values) {
        Schema schema = api.getComponents().getSchemas().get(name);
        if (schema != null) schema.setEnum(java.util.List.of(values));
    }
}
