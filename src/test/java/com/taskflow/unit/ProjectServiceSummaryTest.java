package com.taskflow.unit;

import com.taskflow.dto.ProjectSummaryResponse;
import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ProjectService;
import com.taskflow.exception.TaskValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceSummaryTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void resumen_proyectoConTareas_calculaContadores() throws TaskValidationException {
        Project p = new Project(2L, "App Móvil", "desc", 1L, LocalDate.now());

        Task t1 = new Task(10L, "T-1", null, TaskStatus.TODO, Priority.MED, 2L, null, null);
        Task t2 = new Task(11L, "T-2", null, TaskStatus.IN_PROGRESS, Priority.MED, 2L, null, LocalDate.now().minusDays(1));
        Task t3 = new Task(12L, "T-3", null, TaskStatus.IN_PROGRESS, Priority.MED, 2L, null, null);
        Task t4 = new Task(13L, "T-4", null, TaskStatus.DONE, Priority.MED, 2L, null, LocalDate.now().minusDays(2));

        List<Task> tareas = List.of(t1, t2, t3, t4);

        when(taskRepository.findByProjectId(2L)).thenReturn(tareas);

        ProjectSummaryResponse res = projectService.resumen(p);

        assertThat(res.projectId()).isEqualTo(2L);
        assertThat(res.projectName()).isEqualTo("App Móvil");
        assertThat(res.totalTasks()).isEqualTo(4);
        assertThat(res.byStatus()).containsEntry("TODO", 1).containsEntry("IN_PROGRESS", 2).containsEntry("DONE", 1);
        assertThat(res.overdue()).isEqualTo(1);
    }

    @Test
    void resumen_proyectoSinTareas_devuelveCeros() {
        Project p = new Project(3L, "Empty", "desc", 1L, LocalDate.now());
        when(taskRepository.findByProjectId(3L)).thenReturn(List.of());

        ProjectSummaryResponse res = projectService.resumen(p);

        assertThat(res.totalTasks()).isEqualTo(0);
        assertThat(res.byStatus()).containsEntry("TODO", 0).containsEntry("IN_PROGRESS", 0).containsEntry("DONE", 0);
        assertThat(res.overdue()).isEqualTo(0);
    }
}
