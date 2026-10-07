# Informe de finalización del backend ArquiTech

> Informe histórico de la primera entrega. Para la ampliación actual en main (asistencia y eliminación de proyectos), consultar [project-deletion-attendance.md](project-deletion-attendance.md).

## Resultado y alcance

Se refactorizó el backend existente en **feature/backend-completion**, creada desde **develop**, a su vez creada desde **main**.
Main y develop conservan el commit base `34e4a6fa5206b47de4d35aa68975b67a6ae2fa6e`.
No se hizo push, merge ni despliegue.

Se conservan Java 17, Spring Boot 3.5.0, MySQL, Maven, JWT Bearer, BCrypt, JPA, auditing, Swagger y los módulos existentes.
La integración final también sincronizó Frontend Web y Project Report. Se añadieron pruebas de integración con
H2; no se añadieron ProjectMember, Attendance ni una entidad persistente WeeklyReport.

Commit de entrega: `feat(backend): complete REST contracts and project authorization`.
Consultar su hash con `git log -1 --format="%h %s" feature/backend-completion`; también se entrega en el resumen final.

## Fuentes contrastadas

- [Frontend, revisión 8330d9e7caf42ce8d7eef96bfeb65b7c22bec379](https://github.com/UPC-1ASI0732-202620-9112-ArquiTech/ArquiTech-FrontendWeb/tree/8330d9e7caf42ce8d7eef96bfeb65b7c22bec379): los ocho modelos, servicios, formularios y acciones de completar/resolver.
- [Contrato frontend](https://github.com/UPC-1ASI0732-202620-9112-ArquiTech/ArquiTech-FrontendWeb/blob/8330d9e7caf42ce8d7eef96bfeb65b7c22bec379/docs/api-contract.md).
- [Trazabilidad HU/TS del frontend](https://github.com/UPC-1ASI0732-202620-9112-ArquiTech/ArquiTech-FrontendWeb/blob/8330d9e7caf42ce8d7eef96bfeb65b7c22bec379/docs/user-stories-traceability.md).
- [Project Report](https://github.com/UPC-1ASI0732-202620-9112-ArquiTech/ArquiTech-Report/blob/main/README.md): historias funcionales y técnicas relacionadas, consultadas en modo lectura.
- Las instrucciones explícitas de esta tarea delimitan el alcance frente a funcionalidades documentales antiguas.

## Arquitectura final

Controller → Resource validado → Command → Application Service → Domain → Repository → MySQL.

Las consultas se separan de las operaciones de escritura. Los controladores no acceden a repositories ni construyen entidades falsas con IDs.
Los DTO de respuesta solo exponen el contrato REST; el User de JPA no se serializa.
Las dependencias se inyectan por constructor. Un `Clock` inyectable determina el cierre de tareas e incidencias.

Contextos:

| Contexto | Implementación |
| --- | --- |
| IAM | Usuarios, SUPERVISOR/CONTRACTOR, autenticación y JWT con email |
| Project Management | Proyectos, relaciones supervisor/contratante, acceso reutilizable |
| Inventory | Materiales, movimientos persistidos y maquinaria |
| Workforce | Trabajadores y tareas |
| Incidents | Incidencias y PDF individual conservado |
| Shared | Auditing, errores REST, configuración, reloj y OpenAPI |

## Entidades y cambios de base de datos

| Entidad / tabla | Cambios |
| --- | --- |
| User / users | Nuevos `phone`, `created_at`; índice único `uk_users_email`. `name` sigue físicamente igual y se expone como `fullName`. |
| Project / projects | Nuevos `location`, `progress`. Campo Java `supervisor` sobre `user_id`; contratante sobre `contractor_id`. |
| Material / materials | Nuevos `stock`, `minimum_stock`. Se conservan las cantidades/columnas legacy sin exponerlas. |
| MaterialMovement / material_movements | Tabla nueva: `id`, `material_id`, `type`, `quantity`, `supplier`, `registered_by_user_id`, `occurred_at`, `note`. FKs a material y usuario. Proyecto/nombre/unidad se derivan del material. |
| Machinery / machineries | Nueva `description`; Java `serialNumber` sobre `license_plate`, `registeredAt` sobre `register_date`. Se mantiene unicidad de serie. |
| Worker / workers | Nuevos `specialty`, `status`; Java `fullName` conserva `name`, `hireDate` conserva `hired_date`. Se reutilizan WorkerName/WorkerRole. |
| Task / tasks | Nuevos `title`, `completed_at`; descripción ampliada a 400 caracteres. Se conservan `id_project`, `id_worker`, `start_date`. |
| Incident / incidents | Nuevos `reported_by_user_id`, `reported_at`, `resolved_at`; descripción ampliada a 500. Java `type` mantiene `incident_type`; `date` y `measures_taken` se conservan. |

Los estados actuales se añadieron a los enums sin retirar los valores persistidos antiguos.
Se mantiene `AuditableAbstractAggregateRoot` y su auditing; User usa callback para su nueva fecha de creación.
No se introduce Flyway. La conversión decimal se documenta mediante
`docs/migrations/2026-10-material-quantities-decimal.sql`, un script no destructivo para ejecución controlada.

### Compatibilidad con Railway y datos históricos

- No se conectó ni modificó una base Railway durante este trabajo.
- `ddl-auto=update` sigue disponible por defecto y se hace configurable mediante `DDL_AUTO`.
- Las columnas nuevas en tablas existentes permiten valores nulos para admitir filas antiguas.
- Material con `stock=null`: se interpreta el saldo legacy como `max(0, quantity - quantity_exit)`. La primera entrada/salida guarda el saldo nuevo; los movimientos posteriores usan `stock`.
- `minimumStock` y `progress` nulos se exponen como cero. Worker.status nulo se expone como ACTIVE.
- Task.title faltante usa la descripción. No se inventa completedAt de una tarea antigua.
- Incident.reportedAt faltante usa su fecha legacy a medianoche UTC; no se inventan autor ni resolvedAt.
- User.createdAt antiguo puede seguir nulo; no se presenta la fecha del despliegue como fecha real de registro.
- Material.date conserva VARCHAR para evitar conversión automática de datos legacy; el DTO lo interpreta como fecha ISO.
- Estados leídos: PAUSED → SUSPENDED; UNDER_MAINTENANCE → MAINTENANCE; AVAILABLE → OPERATIONAL; DONE → COMPLETED; Incident.PENDING → OPEN.
- Estos nombres legacy no se admiten en nuevos comandos funcionales y no aparecen en los enums publicados por Swagger.
- El historial nuevo contiene movimientos realmente registrados. Los campos antiguos no permiten reconstruir de forma fiable todos los movimientos ni sus autores.
- Borrar material elimina explícitamente sus movimientos antes del padre, dentro de la misma transacción.
- Borrar trabajador con tareas devuelve 409 para conservar la integridad de las referencias.

Antes de desplegar sobre una base existente se debe revisar su esquema y datos reales, especialmente correos/series duplicados, fechas legacy no ISO y estados fuera de los enums conocidos.
Las columnas existentes no fueron inspeccionadas en Railway. No se afirma que una actualización de ese esquema haya sido ejecutada o validada.

## Contrato REST y autorización

- JSON camelCase. Aliases de entrada limitados para nombres anteriores; nunca snake_case en respuestas.
- Creaciones 201 con recurso; PUT 200 con recurso; DELETE 204; listas vacías 200 con `[]`.
- Todas las listas se limitan a proyectos accesibles. Los filtros de proyecto se resuelven en SQL.
- SUPERVISOR: crear proyectos a su propio nombre y escribir recursos de proyectos que supervisa.
- CONTRACTOR: consultar proyectos donde aparece como contratante, recursos e historial; 403 al intentar escribir recursos de obra.
- Se comprueba acceso directo e indirecto a materiales, movimientos, maquinaria, trabajadores, tareas e incidencias.
- `GET /users` solo SUPERVISOR. `GET /users/{id}`: supervisor o usuario consultando su propio perfil.
- `GET /projects/supervisor/{userId}` exige que el ID coincida con el Supervisor autenticado.
- `GET /projects` resuelve en servidor el alcance de Supervisor y Contractor; no depende del filtrado Angular.
- Stock y cantidad acumulada cambian solo por operaciones de entrada/uso; PUT no los admite.
- Material entry/use usa bloqueo pesimista y una transacción común con el movimiento; stock insuficiente falla antes de mutar el saldo.
- La asignación de trabajador valida el proyecto. El proyecto de un recurso existente no se puede cambiar mediante PUT.
- completedAt/resolvedAt se gestionan en el dominio con el reloj del servidor. Los valores enviados por el frontend se ignoran.
- reportedByUserId se deriva del JWT; el cliente no puede suplantar al reportante.

### Endpoints finales

21 rutas, 32 operaciones documentadas. La siguiente tabla proviene del OpenAPI generado por la suite final.

| Método | Ruta | Uso |
| --- | --- | --- |
| GET | `/api/v1/workers/{id}` | Read workers by ID |
| PUT | `/api/v1/workers/{id}` | Update workers |
| DELETE | `/api/v1/workers/{id}` | Delete workers |
| GET | `/api/v1/users/{id}` | Read a user profile |
| PUT | `/api/v1/tasks/{id}` | Update tasks |
| DELETE | `/api/v1/tasks/{id}` | Delete tasks |
| PUT | `/api/v1/materials/{id}` | Update descriptive material fields |
| DELETE | `/api/v1/materials/{id}` | Delete material and its movement history |
| GET | `/api/v1/machinery/{id}` | Read machinery by ID |
| PUT | `/api/v1/machinery/{id}` | Update machinery |
| DELETE | `/api/v1/machinery/{id}` | Delete machinery |
| PUT | `/api/v1/incidents/{id}` | Update an incident and its resolution status |
| DELETE | `/api/v1/incidents/{id}` | Delete an incident |
| GET | `/api/v1/workers` | List accessible workers, optionally by project |
| POST | `/api/v1/workers` | Create workers in a supervised project |
| GET | `/api/v1/tasks` | List accessible tasks, optionally by project |
| POST | `/api/v1/tasks` | Create tasks in a supervised project |
| GET | `/api/v1/projects` | List your accessible projects |
| POST | `/api/v1/projects` | Create a project as its supervisor |
| POST | `/api/v1/materials` | Create material and initial entry |
| POST | `/api/v1/materials/{id}/use` | Consume stock and record usage |
| POST | `/api/v1/materials/{id}/entry` | Receive stock and record an entry |
| GET | `/api/v1/machinery` | List accessible machinery, optionally by project |
| POST | `/api/v1/machinery` | Create machinery in a supervised project |
| POST | `/api/v1/incidents` | Report an incident as the authenticated supervisor |
| POST | `/api/v1/authentication/sign-up` | Register a supervisor or contractor |
| POST | `/api/v1/authentication/sign-in` | Sign in using email and password |
| GET | `/api/v1/users` | List users for project assignment |
| GET | `/api/v1/projects/supervisor/{userId}` | List your supervised projects |
| GET | `/api/v1/materials/project/{projectId}` | List materials in a project |
| GET | `/api/v1/materials/project/{projectId}/history` | List all project material movements |
| GET | `/api/v1/incidents/project/{projectId}` | List incidents in a project |

El API público no conserva endpoints legacy de materiales ni el PDF individual de incidencia.
El reporte semanal continúa consolidándose y generándose en Angular.

## Seguridad y errores

- JWT subject, UserDetails.username y búsqueda de usuario usan email.
- Se eliminaron logs del header Authorization, tokens y excepciones JWT con contenido sensible.
- BCrypt sigue codificando nuevas contraseñas; límite de 72 bytes UTF-8.
- JWT_SECRET y contraseña MySQL no tienen secretos de fallback.
- Stateless y CSRF deshabilitado para REST JWT.
- Matchers públicos exactos de sign-in/sign-up; Swagger público. El resto exige autenticación.
- Reglas de rol en la cadena HTTP y `@PreAuthorize` de servicios; acceso por proyecto centralizado.
- User.password tiene `@JsonIgnore` como defensa adicional; todos los endpoints usan DTO.
- CORS acepta orígenes explícitos configurables, no comodines.
- `open-in-view=false`, SQL logging desactivado por defecto.

Formato común:

```json
{"code":"INSUFFICIENT_STOCK","message":"Insufficient stock","timestamp":"2026-10-01T15:00:00Z","path":"/api/v1/materials/1/use"}
```

Manejo global para validación, JSON inválido, tipos/parámetros incorrectos, autenticación, autorización,
recursos inexistentes, integridad, concurrencia y errores inesperados. No se envían stack traces.

Códigos: INSUFFICIENT_STOCK, INVALID_CREDENTIALS, DUPLICATED_SERIAL_NUMBER, WORKER_NOT_FOUND,
INVALID_CONTRACTOR, VALIDATION_ERROR, NOT_FOUND, FORBIDDEN, UNAUTHORIZED, EMAIL_ALREADY_EXISTS,
PROJECT_NOT_FOUND, MATERIAL_NOT_FOUND, TASK_NOT_FOUND, INCIDENT_NOT_FOUND, MACHINERY_NOT_FOUND,
WORKER_HAS_TASKS, DATA_CONFLICT, CONCURRENT_MODIFICATION, INTERNAL_ERROR.

400: reglas/validación; 401: autenticación; 403: autorización; 404: inexistente; 409: conflicto.

## Configuración y dependencias

Variables conservadas: DATABASE_URL, PROD_DB_USERNAME, PROD_DB_PASSWORD, JWT_SECRET, JWT_EXPIRATION_DAYS, PORT.
Nuevas: **CORS_ALLOWED_ORIGINS**, **SHOW_SQL**, **DDL_AUTO**.
La configuración completa y ejecución local están en [README](../README.md).

Maven Wrapper reparado agregando `.mvn/wrapper/maven-wrapper.properties`, Maven 3.9.9.
Se retiró la dependencia redundante jakarta.validation-api; se declaró commons-lang3 usado por el contrato de autenticación.
iText se conserva en 8.0.5 y se usa su coordenada actual itext-core, indicada por la advertencia de reubicación Maven.
Spring Boot permanece en 3.5.0.

## Resultado exacto de validación

Comando ejecutado con JDK **17.0.20.1**, Maven **3.9.9**:

```text
./mvnw.cmd -B -ntp clean package -DskipTests
[INFO] Compiling 133 source files with javac [debug parameters release 17]
[INFO] Compiling 1 source file with javac [debug parameters release 17]
[INFO] Tests are skipped.
[INFO] BUILD SUCCESS
[INFO] Total time:  9.932 s
[INFO] Finished at: 2026-10-01T12:43:31-05:00
Exit code: 0
```

Artefacto: `target/arquitech-back-end-0.0.1-SNAPSHOT.jar`.
Solo se emitió advertencia de compilación por referencias a constantes deprecadas conservadas para compatibilidad.

Arranque final:

```text
Spring Boot 3.5.0 / Java 17.0.20.1
Found 8 JPA repository interfaces.
Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 18080
Started ArquitechBackEndApplication in 7.598 seconds
```

Se utilizaron una URL MySQL local deliberadamente no operativa, credenciales efímeras,
`ddl-auto=none`, `hibernate.boot.allow_jdbc_metadata_access=false` y
`hikari.initialization-fail-timeout=-1`.
Esto verifica carga del contexto, mapeos JPA, consultas declaradas, controladores y Swagger **sin verificar conexión ni persistencia MySQL**.
No se usaron credenciales de producción. El proceso temporal fue detenido al terminar.

Comprobaciones de la aplicación iniciada:

- GET /v3/api-docs accesible y título ArquiTech REST API.
- 32 operaciones, 21 rutas; POST documentados con 201 y esquema del recurso.
- Seguridad de sign-in/sign-up vacía; Bearer global para los demás.
- Enums publicados sin valores legacy y ninguna propiedad snake_case.
- Password aparece solo en DTO de entrada de autenticación, nunca en DTO de respuesta.
- GET /api/v1/projects sin token: 401 con ErrorResponse JSON.
- Preflight CORS de http://localhost:4200: 200 y Access-Control-Allow-Origin exacto.
- Revisión de @PathVariable, separación de servicios, referencias eliminadas y ausencia de logs JWT.
- `git diff --check` sin errores. La prueba existente no se modificó.

La suite `ApiIntegrationTests` ejecuta cuatro escenarios end-to-end con múltiples operaciones y aserciones sobre
autenticación, roles/scoping, Projects, Materials, Machinery, Workers, Tasks, Incidents, Users y OpenAPI.
Usa H2 en modo MySQL y no depende de secretos ni de MySQL del desarrollador.

## Pendientes y cambios posteriores del frontend

Pendientes de entorno/segunda etapa:

1. Validar el esquema existente y el arranque con una instancia MySQL real antes del despliegue.
2. Revisar/calibrar datos legacy faltantes. No se pueden deducir autores ni fechas de cierre históricas.
3. Ejecutar las pruebas planificadas de persistencia, concurrencia, permisos, errores y flujos completos cuando se autorice esa etapa.
4. Configurar las variables reales en Railway y desplegar esta rama mediante el flujo de integración del equipo.

Cambios posteriores del frontend, sin editarlo en esta tarea:

- Configurar `apiBaseUrl`, el origen en CORS y `useMockApi: false` después de desplegar.
- El perfil continúa almacenándose localmente; SP-03 evaluará una migración posterior.
- GET /projects ya filtra correctamente para Contractor y Supervisor.
- Eliminar filtros redundantes de seguridad en maquinaria/trabajadores/tareas cuando el equipo lo considere; el servidor ya aplica el alcance.
- Dejar de enviar completedAt/resolvedAt y autor de incidencia; se aceptan por compatibilidad pero los decide el servidor.
- Adaptar el borrado de trabajador a 409 WORKER_HAS_TASKS: reasignar/eliminar tareas antes.
- Los formularios de tareas e incidencias existentes ya son compatibles con las longitudes y descripción opcional revisadas.
- Mantener los guards como ayuda de navegación; el backend valida permisos independientemente.
- Los reportes semanales históricos pueden carecer de movimientos/fechas anteriores a esta versión.

## Inventario de archivos

Rutas relativas a `arquitech-back-end/`. Archivos creados: 31;
modificados: 95; eliminados: 13.

Las eliminaciones corresponden a interfaces/queries/facade sin referencias, un modelo de auditoría duplicado,
el evento de material sin consumidores y recursos legacy sustituidos. Se revisaron sus referencias antes de retirarlos.

### Creados

- `.mvn/wrapper/maven-wrapper.properties`
- `README.md`
- `docs/backend-completion.md`
- `src/main/java/com/acme/arquitech/platform/iam/application/internal/authorization/CurrentUserService.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/UpdateUserResource.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/model/commands/CreateIncidentCommand.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/model/commands/UpdateIncidentCommand.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/model/commands/CreateMachineryCommand.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/model/commands/UpdateMachineryCommand.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/aggregates/MaterialMovement.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/commands/CreateMaterialCommand.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/commands/MaterialEntryCommand.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/commands/MaterialUsageCommand.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/commands/UpdateMaterialCommand.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/valueobjects/MovementType.java`
- `src/main/java/com/acme/arquitech/platform/materials/infrastructure/persistence/jpa/repositories/MaterialMovementRepository.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/LowInventoryResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/MaterialEntryResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/MaterialMovementResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/MaterialUsageResource.java`
- `src/main/java/com/acme/arquitech/platform/projects/application/authorization/ProjectAccessService.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/model/commands/CreateProjectCommand.java`
- `src/main/java/com/acme/arquitech/platform/shared/domain/exceptions/ApiException.java`
- `src/main/java/com/acme/arquitech/platform/shared/infrastructure/configuration/TimeConfiguration.java`
- `src/main/java/com/acme/arquitech/platform/shared/interfaces/rest/GlobalExceptionHandler.java`
- `src/main/java/com/acme/arquitech/platform/shared/interfaces/rest/resources/ErrorResponse.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/model/commands/CreateTaskCommand.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/model/commands/UpdateTaskCommand.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/commands/CreateWorkerCommand.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/commands/UpdateWorkerCommand.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/valueobjects/WorkerStatus.java`

### Modificados

- `pom.xml`
- `src/main/java/com/acme/arquitech/platform/ArquitechBackEndApplication.java`
- `src/main/java/com/acme/arquitech/platform/iam/application/internal/commandservices/UserCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/iam/application/internal/queryservices/UserQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/aggregates/User.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/commands/SignInCommand.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/commands/SignUpCommand.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/valueobjects/Role.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/services/UserCommandService.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/services/UserQueryService.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/authorization/sfs/configuration/WebSecurityConfiguration.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/authorization/sfs/model/UserDetailsImpl.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/authorization/sfs/pipeline/BearerAuthorizationRequestFilter.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/authorization/sfs/pipeline/UnauthorizedRequestHandlerEntryPoint.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/hashing/bcrypt/BCryptHashingService.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/tokens/jwt/BearerTokenService.java`
- `src/main/java/com/acme/arquitech/platform/iam/infrastructure/tokens/jwt/services/TokenServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/AuthenticationController.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/UsersController.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/AuthenticatedUserResource.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/SignInResource.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/SignUpResource.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/UserResource.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/transform/SignUpCommandFromResourceAssembler.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/transform/UserResourceFromEntityAssembler.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/exceptions/IncidentNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/model/aggregates/Incident.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/model/valueobjects/IncidentStatus.java`
- `src/main/java/com/acme/arquitech/platform/incidents/domain/services/IncidentService.java`
- `src/main/java/com/acme/arquitech/platform/incidents/interfaces/rest/IncidentController.java`
- `src/main/java/com/acme/arquitech/platform/incidents/internal/commandservices/IncidentCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/incidents/internal/queryservices/IncidentQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/incidents/repositories/IncidentRepository.java`
- `src/main/java/com/acme/arquitech/platform/incidents/rest/resources/CreateIncidentResource.java`
- `src/main/java/com/acme/arquitech/platform/incidents/rest/resources/IncidentResource.java`
- `src/main/java/com/acme/arquitech/platform/incidents/rest/resources/UpdateIncidentResource.java`
- `src/main/java/com/acme/arquitech/platform/machinery/application/internal/commandservices/MachineryCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/machinery/application/internal/queryservices/MachineryQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/exception/MachineryNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/model/aggregates/Machinery.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/model/valueobjects/MachineryStatus.java`
- `src/main/java/com/acme/arquitech/platform/machinery/domain/service/MachineryService.java`
- `src/main/java/com/acme/arquitech/platform/machinery/infrastructure/persistence/jpa/repositories/MachineryRepository.java`
- `src/main/java/com/acme/arquitech/platform/machinery/interfaces/rest/MachineryController.java`
- `src/main/java/com/acme/arquitech/platform/machinery/interfaces/rest/resources/CreateMachineryResource.java`
- `src/main/java/com/acme/arquitech/platform/machinery/interfaces/rest/resources/MachineryResource.java`
- `src/main/java/com/acme/arquitech/platform/machinery/interfaces/rest/resources/UpdateMachineryResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/application/internal/commandservices/MaterialCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/materials/application/internal/queryservices/MaterialQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/exception/InsufficientStockException.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/exception/InvalidMaterialDataException.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/exception/MaterialNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/aggregates/Material.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/service/MaterialService.java`
- `src/main/java/com/acme/arquitech/platform/materials/infrastructure/persistence/jpa/repositories/MaterialRepository.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/MaterialController.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/CreateMaterialResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/MaterialResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/UpdateMaterialResource.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/exceptions/ProjectNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/model/aggregates/Project.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/model/valueobjects/ProjectStatus.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/services/ProjectCommandService.java`
- `src/main/java/com/acme/arquitech/platform/projects/domain/services/ProjectQueryService.java`
- `src/main/java/com/acme/arquitech/platform/projects/infrastructure/persistence/jpa/repositories/ProjectRepository.java`
- `src/main/java/com/acme/arquitech/platform/projects/interfaces/rest/ProjectController.java`
- `src/main/java/com/acme/arquitech/platform/projects/interfaces/rest/resources/CreateProjectResource.java`
- `src/main/java/com/acme/arquitech/platform/projects/interfaces/rest/resources/ProjectResource.java`
- `src/main/java/com/acme/arquitech/platform/projects/internal/commandservices/ProjectCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/projects/internal/queryservices/ProjectQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/shared/infrastructure/documentation/openapi/configuration/OpenApiConfiguration.java`
- `src/main/java/com/acme/arquitech/platform/tasks/application/internal/commandservices/TaskCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/tasks/application/internal/queryservices/TaskQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/exceptions/TaskNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/model/aggregates/Task.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/model/valueobjects/TaskStatus.java`
- `src/main/java/com/acme/arquitech/platform/tasks/domain/services/TaskService.java`
- `src/main/java/com/acme/arquitech/platform/tasks/infrastructure/persistence/jpa/repositories/TaskRepository.java`
- `src/main/java/com/acme/arquitech/platform/tasks/interfaces/rest/TaskController.java`
- `src/main/java/com/acme/arquitech/platform/tasks/interfaces/rest/resources/CreateTaskResource.java`
- `src/main/java/com/acme/arquitech/platform/tasks/interfaces/rest/resources/TaskResource.java`
- `src/main/java/com/acme/arquitech/platform/tasks/interfaces/rest/resources/UpdateTaskResource.java`
- `src/main/java/com/acme/arquitech/platform/workers/application/internal/commandservices/WorkerCommandServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/workers/application/internal/queryservices/WorkerQueryServiceImpl.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/exceptions/WorkerNotFoundException.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/aggregates/Worker.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/valueobjects/WorkerName.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/model/valueobjects/WorkerRole.java`
- `src/main/java/com/acme/arquitech/platform/workers/domain/services/WorkerService.java`
- `src/main/java/com/acme/arquitech/platform/workers/infrastructure/persistence/jpa/repositories/WorkerRepository.java`
- `src/main/java/com/acme/arquitech/platform/workers/interfaces/rest/WorkerController.java`
- `src/main/java/com/acme/arquitech/platform/workers/interfaces/rest/resources/CreateWorkerResource.java`
- `src/main/java/com/acme/arquitech/platform/workers/interfaces/rest/resources/UpdateWorkerResource.java`
- `src/main/java/com/acme/arquitech/platform/workers/interfaces/rest/resources/WorkerResource.java`
- `src/main/resources/application.properties`

### Eliminados

- `src/main/java/com/acme/arquitech/platform/iam/domain/model/commands/SeedRolesCommand.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/queries/GetAllRolesQuery.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/queries/GetAllUsersQuery.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/queries/GetUserByIdQuery.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/model/queries/GetUserByUsernameQuery.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/services/RoleCommandService.java`
- `src/main/java/com/acme/arquitech/platform/iam/domain/services/RoleQueryService.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/acl/IamContextFacade.java`
- `src/main/java/com/acme/arquitech/platform/iam/interfaces/rest/resources/RoleResource.java`
- `src/main/java/com/acme/arquitech/platform/materials/domain/model/events/MaterialUsedEvent.java`
- `src/main/java/com/acme/arquitech/platform/materials/interfaces/rest/resources/MaterialTransactionResource.java`
- `src/main/java/com/acme/arquitech/platform/shared/domain/model/entities/AuditableModel.java`
- `src/main/java/com/acme/arquitech/platform/shared/interfaces/rest/resources/MessageResource.java`
