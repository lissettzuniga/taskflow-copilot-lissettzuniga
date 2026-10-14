# Arquitectura de TaskFlow

Bienvenido — este documento explica en alto nivel la arquitectura para un nuevo desarrollador.

1) Capas y paquetes

- Paquete raíz: `com.taskflow`.
- Capas convencionales:
  - Controladores (HTTP): `src/main/java/com/taskflow/controller` — p.ej. `TaskController`, `ProjectController`.
  - Servicio (casos de uso / orquestación): `src/main/java/com/taskflow/service` — p.ej. `TaskService`, `ProjectService`.
  - Repositorio (persistencia): `src/main/java/com/taskflow/repository` — p.ej. `TaskRepository`, `ProjectRepository`.
  - Modelo/dominio (entidades con reglas): `src/main/java/com/taskflow/model` — p.ej. `Task`, `Project`.
  - DTOs y mappers: `src/main/java/com/taskflow/dto` y `src/main/java/com/taskflow/mapper` — p.ej. `TaskMapper`.
  - Seguridad/config: `src/main/java/com/taskflow/config` y `src/main/java/com/taskflow/security` — p.ej. `SecurityConfig`, `ProjectSecurity`.
  - Advice/errores: `src/main/java/com/taskflow/advice` — p.ej. `GlobalExceptionHandler`.
  - Excepciones: `src/main/java/com/taskflow/exception` — p.ej. `TaskValidationException`, `TaskNotFoundException`. 

2) Recorrido de POST /projects/{projectId}/tasks (flujo completo)

- Petición HTTP llega a `TaskController` (`src/main/java/com/taskflow/controller/TaskController.java`).
- `TaskController.createTask` valida el DTO (`@Valid`), extrae `projectId` del path y comprueba que el proyecto existe llamando a `ProjectService.buscarPorId` (si no existe lanza `ProjectNotFoundException` -> 404).
- `TaskController` llama a `TaskService.crear(TaskRequest, projectId)` (`src/main/java/com/taskflow/service/TaskService.java`). `TaskService.crear` convierte el DTO en entidad nueva mediante `TaskMapper.aEntidadNueva` (`src/main/java/com/taskflow/mapper/TaskMapper.java`) y persiste la entidad con `TaskRepository.save` (`src/main/java/com/taskflow/repository/TaskRepository.java`). El repositorio es una interfaz `JpaRepository<Task, Long>`; Spring Data implementa su proxy en tiempo de ejecución.
- `TaskService.crear` devuelve la entidad persistida; `TaskController.createTask` la convierte al DTO de salida con `TaskMapper.aResponse` y responde `201 Created` con la cabecera `Location` apuntando a `/tasks/{id}`.
- Si `Task.crear` lanza `TaskValidationException`, el `GlobalExceptionHandler` (`src/main/java/com/taskflow/advice/GlobalExceptionHandler.java`) lo mapea a `400 Bad Request`. La traducción de excepciones a códigos HTTP la realiza `GlobalExceptionHandler`, no los servicios.

3) Dónde viven las reglas de negocio

- Reglas de invariantes y validación de negocio que definen el estado válido de una `Task` están en la entidad de dominio: `src/main/java/com/taskflow/model/Task.java` (métodos `crear`, `setStatus`, `estaVencida`, validaciones en constructor/factory).
- Las políticas de orquestación (comprobaciones adicionales, llamadas a varios repositorios o servicios) están en `TaskService` y otros servicios en `src/main/java/com/taskflow/service`.
- Los mappers (`src/main/java/com/taskflow/mapper/TaskMapper.java`) traduce DTO ↔ entidad y deben usar las fábricas/constructores de dominio (no saltarse las reglas).
- No duplicar reglas en el controlador: valida formato y presencia, la lógica de negocio central está en las entidades y servicios.

4) Seguridad (JWT)

- Configuración central: `src/main/java/com/taskflow/config/SecurityConfig.java` — configura la cadena de filtros, rutas públicas y reglas de autorización.
- Componentes y responsabilidades:
  - `AuthController` (`src/main/java/com/taskflow/controller/AuthController.java`): expone `/auth` (register, login, me) y delega en `AuthService`.
  - `AuthService` (`src/main/java/com/taskflow/service/AuthService.java`): implementa `register` (verifica `UserRepository.existsByUsername`, hashea la contraseña con `PasswordEncoder` y guarda el `User`) y `login` (delegación al `AuthenticationManager`; al autenticar, obtiene `UserDetails` y solicita a `JwtService` un token).
  - `JwtService` (`src/main/java/com/taskflow/security/JwtService.java`): genera y valida JWT (claims, firma, expiración).
  - `JwtAuthenticationFilter` (`src/main/java/com/taskflow/security/JwtAuthenticationFilter.java`): extrae el header `Authorization`, valida/parsea el token con `JwtService`, usa el `UserDetailsService` (implementado por `JpaUserDetailsService`) para cargar `UserDetails` y, si es válido, coloca una `Authentication` en el `SecurityContext`. Si el token es inválido o expirado, el filtro responde `401` directamente.
  - `JpaUserDetailsService` (`src/main/java/com/taskflow/service/JpaUserDetailsService.java`): carga el `User` desde `UserRepository` y construye un `UserDetails` con `username`, `passwordHash` y roles.
- No hay estado de sesión en servidor; la política es stateless JWT. La revocación de tokens requeriría mecanismos adicionales (listas negras, expiraciones cortas, etc.).
- Autorización por roles y métodos: se usa `@PreAuthorize` y clases de seguridad como `ProjectSecurity` (`src/main/java/com/taskflow/security/ProjectSecurity.java`) para decisiones basadas en ownership o roles.

5) Organización de tests

- Tests unitarios (rápidos, sin Spring): `src/test/java/com/taskflow/unit` — p.ej. `TaskServiceTest`, `TaskValidationTest`. Usan JUnit 5 y Mockito; prueban lógica de negocio y servicios aislados.
- Tests slice: `src/test/java/com/taskflow/slice` — p.ej. `TaskControllerTest`, `ProjectControllerTest`. Incluye slices de persistencia con `@DataJpaTest` (rápidos, usan H2) y slices web con `@WebMvcTest`.
- Tests de integración: `src/test/java/com/taskflow/integration` — pruebas con `@SpringBootTest`. `mvn test` ejecuta las pruebas de integración cuyos nombres encajan con los patrones normales (p. ej. `*Test`); los `*IT.java` que usan Testcontainers (por ejemplo `TaskRepositoryPostgresIT`) quedan excluidos salvo que se active `mvn test -Ddocker.tests=true` (perfil `docker-it`).
- Convenciones importantes:
  - No modificar tests existentes para que pasen; arreglar el código si fallan.
  - Ejecutar la suite tras cambios: `mvn -q test` (o un único test con `-Dtest=...`).

6) Puntos prácticos rápidos

- DTOs como `record` con `@Valid` en `@RequestBody`.
- Controladores → servicios → repositorios; nunca exponer entidades JPA en respuestas (usar DTOs y mappers).
- Reglas de BD/ORM: `Task` usa `@Enumerated(STRING)` para enums y relaciones `@ManyToOne(fetch = LAZY)` con el escalar `projectId` como columna dueña.
- Manejo de errores centralizado en `GlobalExceptionHandler` (`src/main/java/com/taskflow/advice/GlobalExceptionHandler.java`) con códigos:
  - 400 validación, 404 no encontrado, 422 estado inválido, 409 conflicto, 401/403 autenticación/autorización.

Archivos/clases clave (referencias):
- `src/main/java/com/taskflow/controller/TaskController.java` (`TaskController`)
- `src/main/java/com/taskflow/controller/ProjectController.java` (`ProjectController`)
- `src/main/java/com/taskflow/service/TaskService.java` (`TaskService`)
- `src/main/java/com/taskflow/service/ProjectService.java` (`ProjectService`)
- `src/main/java/com/taskflow/repository/TaskRepository.java` (`TaskRepository`)
- `src/main/java/com/taskflow/repository/ProjectRepository.java` (`ProjectRepository`)
- `src/main/java/com/taskflow/model/Task.java` (`Task`)
- `src/main/java/com/taskflow/mapper/TaskMapper.java` (`TaskMapper`)
- `src/main/java/com/taskflow/config/SecurityConfig.java` (`SecurityConfig`)
- `src/main/java/com/taskflow/security/ProjectSecurity.java` (`ProjectSecurity`)
- `src/main/java/com/taskflow/advice/GlobalExceptionHandler.java` (`GlobalExceptionHandler`)

---
Documento generado automáticamente para onboarding de desarrolladores.