# ArquiTech Backend

Backend existente de ArquiTech: Java 17, Spring Boot **3.5.0**, Maven, MySQL, JPA, Spring Security, JWT, BCrypt y springdoc-openapi.

## Ejecutar localmente

1. Instalar un JDK 17. No es necesario cambiar el `JAVA_HOME` global de Windows.
2. Disponer de MySQL 8 y una base `arquitechbackend`. Crear una cuenta con permisos sobre esa base.
3. Definir las variables de entorno de la tabla siguiente en la terminal o configuración de ejecución del IDE.
4. Desde esta carpeta, la opción recomendada en Windows es:

```powershell
.\run-backend.ps1
```

El script usa `JAVA17_HOME` si existe; de lo contrario busca JDK 17 en Eclipse Adoptium,
`Program Files\Java` y Microsoft. Valida la versión, cambia `JAVA_HOME`/`PATH` sólo para el
proceso actual, solicita `PROD_DB_PASSWORD` de forma segura y genera un `JWT_SECRET` temporal
si no está definido. Java 25 u otra versión global no se modifica.

También puede ejecutarse manualmente con un terminal que ya use Java 17:

```powershell
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```sh
sh mvnw clean package -DskipTests
sh mvnw spring-boot:run
```

El Wrapper descarga Maven 3.9.9 y las dependencias la primera vez. No requiere Maven global.
También se puede ejecutar `java -jar target/arquitech-back-end-0.0.1-SNAPSHOT.jar`.
Las pruebas de integración usan H2 en modo MySQL y no dependen de credenciales ni de una base MySQL real.

## Variables de entorno

| Variable | Uso / valor predeterminado |
| --- | --- |
| `DATABASE_URL` | URL **JDBC** de MySQL. Local: `jdbc:mysql://localhost:3306/arquitechbackend?serverTimezone=UTC`. Una URL `mysql://...` debe convertirse a formato JDBC. |
| `PROD_DB_USERNAME` | Usuario MySQL; local: `root`. |
| `PROD_DB_PASSWORD` | Contraseña MySQL, obligatoria. Sin valor hardcodeado. |
| `JWT_SECRET` | Secreto aleatorio, obligatorio; al menos 32 bytes UTF-8. Se utiliza el texto del secreto como clave, no se decodifica Base64. Mantener el mismo valor entre instancias. |
| `JWT_EXPIRATION_DAYS` | Vigencia positiva en días; `7`. |
| `PORT` | Puerto HTTP; `8080`. Escucha en `0.0.0.0`. |
| `CORS_ALLOWED_ORIGINS` | Orígenes exactos separados por coma; `http://localhost:4200`. Ejemplo: `http://localhost:4200,https://frontend.example.com`. No acepta comodines. |
| `SHOW_SQL` | SQL logging; `false`. |
| `DDL_AUTO` | Estrategia Hibernate; `update` para mantener compatibilidad con el despliegue actual. |

En Railway, configurar estas variables y ejecutar el JAR generado. La aplicación no carga archivos `.env` automáticamente.
`open-in-view=false`; las relaciones necesarias para los DTO se recuperan mediante JPA dentro de los servicios.

## Swagger

- UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Nombre: **ArquiTech REST API**.
- Obtener un token con `POST /api/v1/authentication/sign-in` y usar **Authorize**.
- `sign-in`, `sign-up` y Swagger son públicos; las otras rutas requieren Bearer JWT.

## Estructura

`src/main/java/com/acme/arquitech/platform` conserva los módulos existentes:

- `iam`: autenticación, usuarios, JWT, BCrypt y usuario actual.
- `projects`: proyectos y `ProjectAccessService`.
- `materials`, `machinery`: inventario y movimientos.
- `workers`, `tasks`: personal y tareas.
- `incidents`: incidencias y su PDF individual.
- `shared`: auditing, reloj inyectable, errores, configuración y OpenAPI.

Flujo: Controller → comando/servicio de aplicación → dominio → repository.
Los DTO de entrada se convierten a comandos; el dominio no depende de los recursos REST.
Los servicios de consulta no crean, modifican ni eliminan entidades.
Se conserva `AuditableAbstractAggregateRoot`.

## Roles y acceso

- **SUPERVISOR** crea proyectos a su propio nombre y administra los recursos de esos proyectos.
- **CONTRACTOR** consulta solamente proyectos donde figura como contratante y sus recursos.
- Las operaciones de escritura de recursos de obra requieren SUPERVISOR, tanto en Spring Security como en los command services.
- La validación de proyecto se aplica a listas, identificadores individuales, movimientos y PDF.
- `GET /users` es exclusivo de SUPERVISOR. Un supervisor puede consultar perfiles para asignar contratantes; los demás usuarios solo su perfil.
- `PUT /users/{id}` acepta exclusivamente `fullName` y `phone`, y requiere que `id` sea el usuario autenticado.
- `USER` se conserva para leer cuentas antiguas; no se admite en nuevos registros ni permite acceder a proyectos.

## Rutas

Todas llevan el prefijo `/api/v1`.

| Recurso | Operaciones |
| --- | --- |
| Authentication | POST `/authentication/sign-in`, POST `/authentication/sign-up` |
| Users | GET `/users`, GET/PUT `/users/{id}` |
| Projects | GET/POST `/projects`, GET `/projects/{id}`, GET `/projects/supervisor/{userId}`, GET `/projects/contractor/{userId}` |
| Materials | GET/POST `/materials`, GET/PUT/DELETE `/materials/{id}`, GET `/materials/project/{projectId}` |
| Material movements | POST `/materials/{id}/entry`, POST `/materials/{id}/use`, GET `/materials/project/{projectId}/history`, GET legacy `/materials/project/{projectId}/history/{materialName}`, GET legacy `/materials/{id}/low-inventory` |
| Machinery | GET/POST `/machinery`, GET/PUT/DELETE `/machinery/{id}` |
| Workers | GET/POST `/workers`, GET/PUT/DELETE `/workers/{id}` |
| Tasks | GET/POST `/tasks`, GET/PUT/DELETE `/tasks/{id}` |
| Incidents | GET/POST `/incidents`, GET/PUT/DELETE `/incidents/{id}`, GET `/incidents/project/{projectId}`, GET `/incidents/{id}/report` |

Las listas de materiales, maquinaria, trabajadores, tareas e incidencias aceptan `?projectId=`.
El filtro se aplica en base de datos y comprueba acceso. Sin filtro, solo se consultan proyectos accesibles.
Los endpoints de supervisor/contratante requieren el ID y rol del propio usuario autenticado.

Creaciones: **201** con recurso. Actualizaciones: **200** con recurso. Eliminaciones: **204**.
Listas vacías: **200** con `[]`. Las respuestas JSON usan **camelCase**.
Fechas de calendario: `YYYY-MM-DD`; movimientos/incidencias: ISO 8601 con offset, normalizado a UTC al persistir.

Rutas legacy conservadas y deprecadas en Swagger:

- GET `/materials/project/{projectId}/history/{materialName}`: devuelve movimientos reales filtrados por nombre.
- GET `/materials/{id}/low-inventory?minimumLevel=...`: devuelve `lowInventory` y el `message` legacy. Sin mínimo usa `minimumStock`.

## Reglas principales

- Proyecto: nombre y ubicación requeridos, presupuesto no negativo, progreso 0–100, fin no anterior al inicio y contratante válido.
- Material: `quantity` es lo recibido acumulado; `stock` es lo disponible. Al crear se registra una entrada inicial, incluso si la cantidad inicial es cero.
- `quantity`, `stock`, `minimumStock` y cantidades de movimientos usan `BigDecimal`/`DECIMAL(19,4)`; `unitPrice` usa `DECIMAL(19,2)`.
- Entrada/uso: cantidad decimal positiva, movimientos y stock en una misma transacción. Bloqueo pesimista por material para evitar operaciones concurrentes sobre el mismo saldo.
- `PUT /materials/{id}` solo modifica descripción del material; rechaza `stock` y `quantity`.
- El RUC sigue la validación del frontend: 11 dígitos empezando por 10, 15, 17 o 20.
- Al borrar material se elimina explícitamente su historial en la misma transacción.
- Número de serie de maquinaria único, incluso en actualización; conflicto `DUPLICATED_SERIAL_NUMBER`.
- El proyecto de maquinaria/trabajadores/tareas/incidencias no se cambia por PUT; si se envía debe coincidir.
- Un trabajador asignado a una tarea debe pertenecer al mismo proyecto. No se puede borrar un trabajador con tareas: `409 WORKER_HAS_TASKS`.
- `completedAt` y `resolvedAt` se fijan al entrar en el estado final, se conservan si permanece y se limpian al reabrir.
- El frontend puede enviar esas fechas por compatibilidad: se ignoran y el servidor calcula las reales.
- El autor de movimientos e incidencias procede del JWT. `reportedByUserId` del cliente no cambia la autoría.
- Los campos desconocidos se rechazan con 400, salvo las excepciones de compatibilidad documentadas.

## Errores

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock",
  "timestamp": "2026-10-01T15:00:00Z",
  "path": "/api/v1/materials/1/use"
}
```

400: validación/reglas. 401: autenticación. 403: acceso. 404: inexistente. 409: duplicados/conflictos.
Códigos principales: `VALIDATION_ERROR`, `INVALID_CREDENTIALS`, `UNAUTHORIZED`, `FORBIDDEN`,
`INVALID_CONTRACTOR`, `INSUFFICIENT_STOCK`, `DUPLICATED_SERIAL_NUMBER`, `EMAIL_ALREADY_EXISTS`,
`WORKER_NOT_FOUND`, `PROJECT_NOT_FOUND`, `MATERIAL_NOT_FOUND`, `TASK_NOT_FOUND`,
`MACHINERY_NOT_FOUND`, `INCIDENT_NOT_FOUND`, `WORKER_HAS_TASKS`, `DATA_CONFLICT`.
No se devuelven entidades User, contraseñas, hashes ni stack traces. No se registran JWT.

## Datos existentes y despliegue

Consultar [el informe del refactor](docs/backend-completion.md) para columnas nuevas, compatibilidad,
límites de la verificación y cambios posteriores del frontend.

Se mantienen las columnas físicas `projects.user_id`, `machineries.license_plate`,
`machineries.register_date`, `workers.name`, `workers.hired_date`, `tasks.id_project`,
`tasks.id_worker`, `tasks.start_date`, `incidents.incident_type` y `incidents.date`.
No se eliminan columnas legacy de materiales.
Los datos históricos no contienen todos los campos nuevos; no se inventan autores o fechas de cierre.

No se implementaron ProjectMember, Attendance ni una entidad/tabla WeeklyReport; no son requeridos por el frontend ni por las historias vigentes.
El reporte semanal sigue a cargo del frontend.

## Operaciones y pruebas

OpenAPI expone **43 operaciones**. `POST /authentication/sign-in` y `sign-up` son públicas;
las otras operaciones requieren Bearer JWT. La suite `ApiIntegrationTests` cubre autenticación,
roles, scoping por proyecto, Materials decimales y transaccionales, CRUD/reglas de maquinaria,
trabajadores, tareas, incidencias, perfil y disponibilidad de `/v3/api-docs`.

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

## Migración de cantidades

Hibernate `DDL_AUTO=update` puede convertir las columnas existentes, pero en producción se recomienda
respaldar la base y ejecutar de forma controlada
[`docs/migrations/2026-10-material-quantities-decimal.sql`](docs/migrations/2026-10-material-quantities-decimal.sql).
La migración normaliza nulos y convierte sin borrar filas.

## Decisión de registro público

Para conservar las demos actuales, `sign-up` todavía permite elegir `SUPERVISOR` o `CONTRACTOR`.
Esto permite autoasignarse privilegios de supervisor y no es apropiado para un alta pública de producción.
Antes de abrir el registro, debe reemplazarse por invitaciones/aprobación administrativa o fijar el rol público permitido.
