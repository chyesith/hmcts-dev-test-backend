package uk.gov.hmcts.reform.dev.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mapper.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

public class TaskServiceImplTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskRequest requestDto;
    private TaskResponse responseDto;
    private Task task;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskMapper = mock(TaskMapper.class);
        taskService = new TaskServiceImpl(taskRepository, taskMapper);
        requestDto =
            new TaskRequest(
                "Sample title 1",
                "sample description for task service implementation test",
                Status.IN_PROGRESS,
                LocalDateTime.now().plusDays(1)
            );

        task =
            new Task(
                1L,
                "Sample title 1",
                "sample description for task service implementation test",
                Status.IN_PROGRESS,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now()
            );

        responseDto =
            new TaskResponse(
                1L,
                "Sample title 1",
                "sample description for task service implementation test",
                Status.IN_PROGRESS,
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
    }

    @Test
    void shouldCreateTaskSuccessfully() {
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(responseDto);

        TaskResponse result = taskService.createTask(requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(taskRepository).save(task);
    }

    @Test
    void shouldThrowExceptionCreatingTaskWithNullInput() {
        assertThrows(NullPointerException.class, () -> taskService.createTask(null));
    }

    @Test
    void shouldGetTaskByIdSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(responseDto);

        TaskResponse result = taskService.getTaskById(1L);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundById() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(99L));
    }

    @Test
    void shouldReturnAllTasks() {
        List<Task> taskList = List.of(task);

        when(taskRepository.findAll()).thenReturn(taskList);
        when(taskMapper.toDto(task)).thenReturn(responseDto);

        List<TaskResponse> result = taskService.getAllTasks();
        assertThat(result).hasSize(1).containsExactly(responseDto);
    }

    @Test
    void shouldUpdateTaskStatusSuccessfully() {
        Task updatedTask =
            new Task(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                Status.COMPLETED,
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
        TaskResponse updatedResponse =
            new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                Status.COMPLETED,
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toDto(updatedTask)).thenReturn(updatedResponse);

        TaskResponse result = taskService.updateTaskStatusById(1L, Status.COMPLETED);
        assertThat(result.status()).isEqualTo(Status.COMPLETED);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(task);
        verify(taskMapper).toDto(updatedTask);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(
            TaskNotFoundException.class, () -> taskService.updateTaskStatusById(2L, Status.COMPLETED));
        verify(taskRepository).findById(2L);
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        when(taskRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(5L));
        verify(taskRepository).findById(5L);
    }
}
