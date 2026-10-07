# Proyectos y asistencia

## Asistencia y eliminación de proyectos (4 de octubre de 2026)

Todas las rutas tienen prefijo /api/v1 y requieren JWT. Supervisor escribe únicamente en sus obras; Contractor consulta las obras donde está asignado. Backend aplica permisos y scope, independientemente de la UI.

| Método | Endpoint | Supervisor | Contractor | Request | Response |
|---|---|---|---|---|---|
| DELETE | /projects/{id} | Propietario | No (403) | Sin body | 204 |
| GET | /attendance?projectId=X&date=YYYY-MM-DD | Leer | Leer | projectId requerido; date opcional | 200: AttendanceResource[] |
| POST | /attendance | Crear | No (403) | projectId, workerId, attendanceDate, status, checkInAt opcional, checkOutAt opcional, notes opcional | 201: AttendanceResource |
| PUT | /attendance/{id} | Editar | No (403) | workerId, attendanceDate, status, checkInAt opcional, checkOutAt opcional, notes opcional | 200: AttendanceResource |
| DELETE | /attendance/{id} | Eliminar | No (403) | Sin body | 204 |

AttendanceResource: id, projectId, workerId, workerName, attendanceDate, status, checkInAt, checkOutAt, notes, registeredByUserId, createdAt, updatedAt.

- Estados: PRESENT, ABSENT, LATE, EXCUSED (presente, ausente, tardanza, justificado).
- attendanceDate es la fecha de la jornada, YYYY-MM-DD. Los instantes de entrada y salida son ISO-8601 UTC; Web y Mobile presentan la hora local y permiten turnos que cruzan medianoche.
- Un solo registro por trabajador/fecha, protegido por constraint de base de datos. Duplicados devuelven 409 DUPLICATE_ATTENDANCE.
- La salida requiere entrada y no puede precederla. ABSENT y EXCUSED no admiten horas. Notes tiene máximo 1000 caracteres.
- El trabajador debe pertenecer a la obra; la fecha no puede preceder su contratación. No se crean registros nuevos para INACTIVE, pero puede corregirse su historial existente.
- No se envían registeredByUserId, workerName, createdAt ni updatedAt: son administrados por el servidor. PUT no admite cambio de projectId.
- Eliminar un trabajador con asistencia devuelve 409 WORKER_HAS_ATTENDANCE; puede marcarse INACTIVE para preservar su historial.
- La eliminación del proyecto elimina asistencia, tareas, trabajadores, movimientos/materiales, maquinaria e incidentes de esa obra; conserva usuarios y otras obras. Todo ocurre en una transacción y se revierte completamente si falla un paso.
- Ambas interfaces requieren escribir el nombre exacto de la obra para confirmar su borrado. El contexto local de esa obra se limpia tras el éxito.
- Las escrituras bloquean el proyecto antes de modificar sus recursos para evitar datos huérfanos durante una eliminación simultánea. Materiales bloquea proyecto antes de material.

## Base de datos

La entidad Attendance crea attendance_records, con FKs a projects, workers y users, auditing y constraint uk_attendance_worker_date. Con DDL_AUTO=update (configuración existente), Hibernate incorpora la tabla al arrancar. Si se administra el esquema manualmente, aplicar docs/migrations/2026-10-attendance.sql antes de iniciar. No se elimina ningún registro al desplegar: los borrados ocurren solo al ejecutar DELETE autorizado.

## Verificación

mvn test ejecuta la suite HTTP real con H2: autorización 401/403, todas las formas REST/OpenAPI, CRUD y filtros de asistencia, validación de fechas/horas, duplicados, campos del servidor, trabajadores inactivos, borrado con dependencias y rollback deliberado. Los tests no utilizan producción.

Resultado final: mvn clean verify aprobado; 7 pruebas de integración sin fallos, contrato OpenAPI de 37 operaciones y JAR ejecutable generado.
