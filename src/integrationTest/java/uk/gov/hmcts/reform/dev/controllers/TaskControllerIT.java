package uk.gov.hmcts.reform.dev.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.models.Status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskControllerIT {

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private  Long createdTaskId;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testDB")
            .withUsername("test_user")
            .withPassword("test123");

    @DynamicPropertySource
    static void initialize(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new task and return 201 response code")
    public void shouldCreateTask() {
        final String taskTitle = "Integration test task";
        final String taskDescription = "sample description for Integration test";

        TaskRequest request = new TaskRequest(
            taskTitle,
            taskDescription,
            Status.IN_PROGRESS,
            LocalDateTime.now().plusDays(1)
        );


        Response response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/tasks")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())                  // Validate ID exists
            .body("title", equalTo(taskTitle))         // Validate title
            .body("description", equalTo(taskDescription))    // Validate description
            .body("status", equalTo(Status.PENDING.name()))
            .extract()
            .response();


        createdTaskId = response.path("id");
    }

    @Test
    @Order(2)
    @DisplayName("Should update created task status to  COMPLETED with 200 response code")
    public void shouldUpdateTaskStatus() throws Exception {
        mockMvc
            .perform(
                patch("/api/v1/cases/{id}/status?status=COMPLETED", createdTaskId, Status.COMPLETED))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @Order(3)
    @DisplayName("Should return requested id task correctly with 200 response code")
    public void shouldGetTaskById() throws Exception {
        mockMvc
            .perform(get("/api/v1/cases/{id}", createdTaskId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdTaskId))
            .andExpect(jsonPath("$.title").value("Integration test case"));
    }

    @Test
    @Order(4)
    @DisplayName("Should return all the tasks with 200 response code")
    public void shouldGetAllTasks() throws Exception {
        mockMvc
            .perform(get("/api/v1/cases"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    public void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();

        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }

    @Test
    @Order(5)
    @DisplayName("Should delete created task  200 response code")
    public void shouldDeleteTask() throws Exception {
        mockMvc
            .perform(delete("/api/v1/cases/{id}", createdTaskId))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        // Verify deletion returns 404
        mockMvc.perform(get("/api/v1/cases/{id}", createdTaskId)).andExpect(status().isNotFound());
    }
}
