package com.taskflow.slice;

import com.taskflow.controller.TaskController;
import com.taskflow.dto.TaskResponse;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.Priority;
import com.taskflow.security.JwtAuthenticationFilter;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReasignarTareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void patchAssignee_existente_devuelve200YcadaCampo() throws Exception {
        Task tarea = new Task(4L, "Tarea4", "d", TaskStatus.TODO, Priority.MED, 1L, null, null);
        when(taskService.buscarPorId(4L)).thenReturn(Optional.of(tarea));
        TaskResponse resp = new TaskResponse(4L, "Tarea4", "d", TaskStatus.TODO, Priority.MED, 1L, 2L, null);
                when(taskService.reasignar(any(Task.class), org.mockito.ArgumentMatchers.eq(2L))).thenReturn(new Task(4L, "Tarea4", "d", TaskStatus.TODO, Priority.MED, 1L, 2L, null));

        mockMvc.perform(patch("/tasks/4/assignee").contentType("application/json").content("{\"assigneeId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(2));
    }

    @Test
    void patchAssignee_tareaInexistente_devuelve404() throws Exception {
        when(taskService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/tasks/99/assignee").contentType("application/json").content("{\"assigneeId\":2}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchAssignee_cuerpoVacio_devuelve400_yNoLlamaService() throws Exception {
        Task tarea = new Task(6L, "Tarea6", "d", TaskStatus.TODO, Priority.MED, 1L, null, null);
        when(taskService.buscarPorId(6L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(patch("/tasks/6/assignee").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());

        org.mockito.Mockito.verify(taskService, never()).reasignar(any(Task.class), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void patchAssignee_tareaDone_devuelve422() throws Exception {
        Task tarea = new Task(2L, "Tarea2", "d", TaskStatus.DONE, Priority.MED, 1L, 3L, LocalDate.now().minusDays(1));
        when(taskService.buscarPorId(2L)).thenReturn(Optional.of(tarea));
        when(taskService.reasignar(any(Task.class), org.mockito.ArgumentMatchers.eq(3L))).thenThrow(new com.taskflow.exception.TaskStateException("No se puede reasignar una tarea terminada."));

        mockMvc.perform(patch("/tasks/2/assignee").contentType("application/json").content("{\"assigneeId\":3}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
