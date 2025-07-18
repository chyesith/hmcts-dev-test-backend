package uk.gov.hmcts.reform.dev;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.containsString;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.models.Status;

import java.time.LocalDateTime;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SmokeTest {
    protected static final String CONTENT_TYPE_VALUE = "application/json";

    @Value("${TEST_URL:http://localhost:4000}")
    private String testUrl;


    private Long createdTaskId;


    @BeforeAll
    void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @Order(1)
    @DisplayName("Create Task")
    void createTask() {
        final String taskTitle = "smoke test task";
        final String taskDescription = "sample description for smoke test";

        TaskRequest requestDto = new TaskRequest(
            taskTitle,
            taskDescription,
            Status.PENDING,
            LocalDateTime.now().plusDays(1)
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(requestDto)
            .when()
            .post("/api/v1/tasks")
            .then()
            .statusCode(201)
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
    @DisplayName("Get Task by ID")
    void getTaskById() {
        given()
            .when()
            .get("/api/v1/tasks/{id}", createdTaskId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdTaskId.intValue()))
            .body("title", equalTo("smoke test get by id task"));
    }

    @Test
    @Order(3)
    @DisplayName("Update Task Status")
    void updateTaskStatus() {
        given()
            .when()
            .patch("/api/v1/tasks/{id}/status?status={status}", createdTaskId, Status.IN_PROGRESS.name())
            .then()
            .statusCode(200)
            .body("status", equalTo(Status.IN_PROGRESS.name()));
    }

    @Test
    @Order(4)
    @DisplayName("Get All Tasks")
    void getAllTasks() {
        given()
            .when()
            .get("/api/v1/tasks")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    @DisplayName("Delete Task")
    void deleteTask() {
        given()
            .when()
            .delete("/api/v1/tasks/{id}", createdTaskId)
            .then()
            .statusCode(204) // Your controller returns 200 OK with a string message
            .body(containsString(""));
    }


}
