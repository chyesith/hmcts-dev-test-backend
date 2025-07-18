package uk.gov.hmcts.reform.dev.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.exception.GlobalExceptionHandler;
import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.services.TaskService;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2025, 10, 1, 12, 0);


    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc =
            MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test-db")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void initialize(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    private TaskRequest sampleTaskRequest(Status status, LocalDateTime dueDate, String title, String description) {
        return new TaskRequest(title, description, status, dueDate);
    }

    private TaskResponse sampleTaskResponse(Long id, Status status, LocalDateTime dueDate) {
        return new TaskResponse(
            id,
            "Sample Task",
            "Sample description",
            status,
            dueDate,
            LOCAL_DATE_TIME,
            LOCAL_DATE_TIME
        );
    }


    @Test
    void shouldCreateTaskSuccessfullyWhenGivenValidTaskRequest() throws Exception {

        TaskRequest request = sampleTaskRequest(
            Status.IN_PROGRESS,
            LOCAL_DATE_TIME.plusDays(1),
            "Sample Task",
            "Sample description"
        );
        TaskResponse response = sampleTaskResponse(1L, Status.IN_PROGRESS, LOCAL_DATE_TIME.plusDays(1));
        when(taskService.createTask(any())).thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.title").value(response.title()))
            .andExpect(jsonPath("$.description").value(response.description()))
            .andExpect(jsonPath("$.status").value(response.status().name()))
            .andExpect(jsonPath("$.dueDate").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnBadRequestWhenGivenNullTitleOrEmptyTaskRequest() throws Exception {

        TaskRequest request1 = sampleTaskRequest(Status.IN_PROGRESS, LOCAL_DATE_TIME.plusDays(1), null, "desc");
        TaskRequest request2 = sampleTaskRequest(
            Status.IN_PROGRESS,
            LOCAL_DATE_TIME.plusDays(1),
            "",
            "Sample description for shouldReturnBadRequestWhenGivenEmptyTaskTitle case"
        );
        mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value("title: Title is a mandatory field"));

        String requestJson = objectMapper.writeValueAsString(request2);

        mockMvc
            .perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value("title: Title is a mandatory field"));
    }


    @Test
    void shouldCreateTaskWhenGivenPastDateRequestWithCompleteStatus() throws Exception {
        TaskRequest request = sampleTaskRequest(
            Status.COMPLETED,
            LOCAL_DATE_TIME.minusDays(2),
            "sample title for case",
            "sample description for test"
        );
        TaskResponse response = sampleTaskResponse(1L, Status.COMPLETED, LOCAL_DATE_TIME.minusDays(2));

        when(taskService.createTask(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.dueDate").exists());

    }

    @Test
    void shouldReturnBadRequestWhenGivenPastDateRequestWithOutCompleteStatus() throws Exception {
        TaskRequest request1 = sampleTaskRequest(Status.IN_PROGRESS, null, "title", "desc");
        TaskRequest request2 = sampleTaskRequest(Status.PENDING, LOCAL_DATE_TIME.minusDays(1), "title", "desc");

        mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value("dueDate: Due date is mandatory for non-completed tasks"));

        mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value("dueDate: Due date should be today or a future date"));
    }

    @Test
    void shouldReturnAllTasksWhenGetAllTasks() throws Exception {
        List<TaskResponse> responseList =
            List.of(
                sampleTaskResponse(1L, Status.IN_PROGRESS, LOCAL_DATE_TIME.plusDays(1)));
        when(taskService.getAllTasks()).thenReturn(responseList);
        mockMvc
            .perform(get("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnCorrectTaskWhenGivenExistingTaskId() throws Exception {
        TaskResponse taskResponse = sampleTaskResponse(1L, Status.IN_PROGRESS, LOCAL_DATE_TIME.plusDays(1));
        when(taskService.getTaskById(1L)).thenReturn(taskResponse);


        mockMvc.perform(get("/api/v1/tasks/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskResponse.id()))
            .andExpect(jsonPath("$.title").value(taskResponse.title()))
            .andExpect(jsonPath("$.status").value(taskResponse.status().name()))
            .andExpect(jsonPath("$.dueDate").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGivenNonExistingTaskId() throws Exception {
        long invalidId = 999L;
        when(taskService.getTaskById(invalidId)).thenThrow(new TaskNotFoundException(invalidId));

        mockMvc.perform(get("/api/v1/tasks/{id}", invalidId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task not found with ID:" + invalidId))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void shouldReturnEmptyListWhenGetEmptyTaskList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldDeleteTaskSuccessfullyThenReturnNoContent() throws Exception {
        long taskId = 1L;

        mockMvc
            .perform(delete("/api/v1/tasks/{id}", taskId))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistentTask() throws Exception {
        long taskId = 999L;
        doThrow(new TaskNotFoundException(taskId)).when(taskService).deleteTask(taskId);
        mockMvc
            .perform(delete("/api/v1/tasks/{id}", taskId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
        long taskId = 999L;

        doThrow(new TaskNotFoundException(taskId)).when(taskService).deleteTask(taskId);

        mockMvc
            .perform(delete("/api/v1/tasks/{id}", taskId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task not found with ID:" + taskId))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void shouldFailCreateTaskWhenRequestBodyIsNull() throws Exception {
        mockMvc
            .perform(
                post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
            .andDo(print())// empty body simulates null
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Required request body is missing"))
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void shouldUpdateTaskStatusSuccessfullyWhenGivenUpdateStatus() throws Exception {
        Long taskId = 1L;
        Status newStatus = Status.COMPLETED;

        TaskResponse updatedResponse = sampleTaskResponse(1L, Status.COMPLETED, LOCAL_DATE_TIME.plusDays(1));

        when(taskService.updateTaskStatusById(taskId, newStatus)).thenReturn(updatedResponse);

        mockMvc
            .perform(
                patch("/api/v1/tasks/{id}/status?status={status}", taskId, newStatus)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.status").value(newStatus.name()))
            .andDo(print());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws Exception {
        Long taskId = 999L;
        Status newStatus = Status.COMPLETED;

        when(taskService.updateTaskStatusById(taskId, newStatus))
            .thenThrow(new TaskNotFoundException(taskId));

        mockMvc
            .perform(
                patch("/api/v1/tasks/{id}/status?status={status}", taskId, newStatus)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Task not found with ID:" + taskId))
            .andDo(print());
    }
}
