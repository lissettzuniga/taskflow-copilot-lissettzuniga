# Proyecto final · Semana 6 · GitHub Copilot

**Alumno:** Lissett Zuñiga Reyes · **Usuario de GitHub:** `lissettzuniga`

## 1. Qué construí

| | Feature | Especificación |
|---|---|---|
| [x] | `PATCH /tasks/{id}/assignee` — cambiar el responsable | [`specs/assignee.md`](../specs/assignee.md) |

El endpoint cambia únicamente el responsable de una tarea. Valida que el identificador sea obligatorio y positivo, rechaza la reasignación de tareas terminadas y devuelve la tarea actualizada.

## 2. El pull request

- **URL del PR mergeado:** https://github.com/lissettzuniga/taskflow-copilot-lissettzuniga/pull/5
- **Commit del merge en main:**

```text
6d3013d Merge pull request #5 from lissettzuniga/feature/assignee
```

- **Comentarios de Copilot code review:** 0 hallazgos o solicitudes de cambio. Publicó el resumen con el veredicto **Approval recommended** y **No open issues**.

## 3. Cómo lo hice

| Paso | Qué hice | Evidencia |
|---|---|---|
| Rama y spec | Creé `feature/assignee` y copié la especificación a `specs/assignee.md`. | Commit `ea644a0`: `spec: assignee (proyecto final)` |
| Implementación | Ejecuté `copilot -p "/crear-endpoint-taskflow …"` con `gpt-5-mini`, permisos de escritura y ejecución de Maven. | [sesion-implementacion.md](sesion-implementacion.md), con la línea `Skill "crear-endpoint-taskflow" loaded successfully`; commit `7fd857e` |
| Revisión | Ejecuté el agente `revisor` sobre el diff y la especificación. No modificó archivos. | [revision.md](revision.md): `Veredicto: APROBADO`; [proyecto-final.diff](proyecto-final.diff) |
| Tests | Ejecuté `mvn test`. Pasé de 83 a 89 tests, agregando dos clases sin modificar tests existentes. | `Tests run: 89, Failures: 0, Errors: 0, Skipped: 0` |
| Comprobación REST | Incorporé `casos-assignee.ps1` al script `verificar.ps1` y lo ejecuté contra la app real. | Sección 5; commit `d535f97` |
| Code review | Solicité la revisión de Copilot en el PR #5 antes del merge. | Pestaña Conversation del PR: `Approval recommended`, sin hallazgos abiertos |

## 4. Qué hizo el agente y qué corregí yo

| # | Observación | Quién la detectó | Qué hice y resultado |
|---|---|---|---|
| 1 | Durante la implementación, el agente ajustó su propio test unitario antes de terminar. | El agente al ejecutar Maven | Lo corrigió dentro de la misma sesión. Después comprobé la suite: 89 tests sin fallos. Quedó incluido en el commit `7fd857e`. |
| 2 | Sugirió eliminar una variable local `TaskResponse resp` que consideró sin uso en `ReasignarTareaControllerTest.java`. | Agente `revisor` | Era una sugerencia opcional, no bloqueante. No la apliqué; el veredicto fue APROBADO. |
| 3 | Sugirió quitar `throws TaskValidationException` de los métodos de `ReasignarTareaServiceTest.java` porque `reasignar` no lanza esa excepción checked. | Agente `revisor` | No apliqué la sugerencia: ese argumento no basta para demostrar que la declaración sobra, pues también debe revisarse la construcción de los datos de prueba. No la registré como un error confirmado. |

No ejecuté prompts de corrección en PF-4 ni PF-6. Copilot code review no solicitó cambios.

Agregué manualmente los casos REST del curso y la línea que los carga desde `verificar.ps1`. También eliminé del transcript las líneas con `generated security password` y comprobé los patrones de secretos indicados en la guía antes del commit.

**Lo que el agente hizo bien sin intervención manual:** implementó el DTO, el método del servicio, el endpoint y las dos clases nuevas de tests dentro del alcance permitido. No tuve que editar el código Java; las comprobaciones REST confirmaron persistencia y respuestas 200, 400, 404, 422 y 401.

## 5. Comprobaciones REST

Salida de `pwsh -NoProfile -File .github/skills/verificar-taskflow/verificar.ps1`, ejecutado en `main` después del merge:

```text
Repositorio: C:\Users\Lenovo\taskflow-copilot-lissettzuniga
URL de la app: http://127.0.0.1:8080
Empaquetando con Maven (mvn -q package -DskipTests), tarda unos segundos...
App arrancando (PID 8324). Esperando a que /info responda...
App lista en 6 s.
[OK]    GET /tasks/overdue devuelve solo la tarea 7
[OK]    GET /tasks/unassigned devuelve las tareas 4 y 6
[OK]    GET /projects/1/summary
[OK]    GET /projects/2/summary
[OK]    GET /projects/3/summary
[OK]    GET /projects/99/summary responde 404
[OK]    GET /projects/1/summary sin token responde 401
[OK]    PATCH /tasks/4/assignee asigna a luis
[OK]    GET /tasks/4 conserva el responsable nuevo
[OK]    PATCH /tasks/4/status a DONE ahora responde 200
[OK]    PATCH /tasks/2/assignee (DONE) responde 422
[OK]    PATCH /tasks/99/assignee responde 404
[OK]    PATCH /tasks/6/assignee con {} responde 400 y nombra assigneeId
[OK]    PATCH /tasks/6/assignee con 0 responde 400
[OK]    PATCH /tasks/6/assignee sin token responde 401
App detenida (PID 8324).
[OK]    App apagada: el puerto 8080 ya no responde
RESULTADO: 16/16 OK
```

Suite ejecutada en `main` después del merge:

```text
[INFO] Tests run: 89, Failures: 0, Errors: 0, Skipped: 0
```

## 6. Créditos de la semana

El consumo mensual se consultó en GitHub Billing el 17 de octubre de 2026. Los consumos de implementación y revisión corresponden a las líneas `AI Credits` de sus respectivas sesiones de CLI.

| Qué | AI credits |
|---|---:|
| Usados en octubre según github.com, incluyendo otras sesiones del mes | 307 |
| Implementación con la skill — PF-2 | 8.86 |
| Revisión del agente revisor — PF-3 | 2.70 |
| Correcciones del PF-4 y del PF-6 | 0 |
| Total registrado de las sesiones CLI del proyecto final | 11.56 |

El plan Copilot Pro muestra 307 de 1,500 AI credits utilizados. El total mensual incluye otras actividades y no equivale al consumo exclusivo de este proyecto.