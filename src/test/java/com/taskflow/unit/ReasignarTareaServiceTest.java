package com.taskflow.unit;

import com.taskflow.exception.TaskStateException;
import com.taskflow.exception.TaskValidationException;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.TaskRepository;
import com.taskflow.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReasignarTareaServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void reasignar_tareaTODO_sinResponsable_guardaConNuevoAssignee() throws TaskValidationException {
        Task tarea = new Task(10L, "Tarea X", "d", TaskStatus.TODO, Priority.MED, 1L, null, null);
        when(repository.save(org.mockito.ArgumentMatchers.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task resultado = service.reasignar(tarea, 5L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());
        assertEquals(5L, captor.getValue().getAssigneeId());
        assertEquals(5L, resultado.getAssigneeId());
    }

    @Test
    void reasignar_tareaDONE_lanzaTaskStateException_yNoGuarda() throws TaskValidationException {
        Task tarea = new Task(11L, "Hecha", "d", TaskStatus.DONE, Priority.MED, 1L, 2L, LocalDate.now().minusDays(1));
        // el servicio recibe la tarea ya encontrada según la spec: no stubbing de findById necesario aquí

        assertThrows(TaskStateException.class, () -> service.reasignar(tarea, 9L));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Task.class));
    }
}
