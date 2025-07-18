package uk.gov.hmcts.reform.dev;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.models.Task;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TaskFunctionalTest {





    private static Long createdTaskId;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + 4000;

        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new task")
    void shouldCreateNewTask() {
        Task task = Task.builder().title("Sample functional Test Task")
            .description("sample description for functional test case")
            .status(Status.PENDING)
            .dueDate(LocalDateTime.now().plusDays(1))
            .build();



        Response response = given()
            .contentType(ContentType.JSON)
            .body(task)
            .when()
            .post("/api/v1/cases")
            .then()
            .extract().response();

        assertEquals(201, response.statusCode());
        createdTaskId = response.jsonPath().getLong("id");
        assertNotNull(createdTaskId);
    }

    @Test
    @Order(2)
    @DisplayName("Should update status of the task")
    void shouldUpdateCreatedTask() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .patch("/api/v1/cases/{id}/status?status=COMPLETED", createdTaskId)
            .then().extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("COMPLETED", response.jsonPath().getString("status"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get all the tasks")
    void shouldGetAllTasks() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/cases")
            .then().extract().response();

        assertEquals(200, response.statusCode());
        assertFalse(response.jsonPath().getList("$").isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Should get task by id")
    void shouldGetById() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/cases/{id}", createdTaskId)
            .then().extract().response();

        assertEquals(200, response.statusCode());
        assertEquals("Functional Test Task", response.jsonPath().getString("title"));
    }

    @Test
    @Order(5)
    @DisplayName("Should delete the task")
    void shouldDelete() {
        Response response = given()
            .contentType(ContentType.JSON)
            .when()
            .delete("/api/v1/cases/{id}", createdTaskId)
            .then().extract().response();

        assertEquals(204, response.statusCode());
    }



}
