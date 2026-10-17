package com.taskflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * TaskAssigneeUpdateRequest — cuerpo de PATCH /tasks/{id}/assignee. DTO por operación: solo
 * contiene el assigneeId, que debe ser un número positivo.
 */
public record TaskAssigneeUpdateRequest(

        @NotNull(message = "El responsable es obligatorio.")
        @Positive(message = "El responsable debe ser un número positivo.")
        Long assigneeId
) {
}
