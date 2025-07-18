package uk.gov.hmcts.reform.dev.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.models.Task;

public class TaskMapperTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setup() {
        taskMapper = new TaskMapper();
    }

    @Test
    void shouldMapRequestDtoToEntity() {
        TaskRequest taskRequest =
            new TaskRequest(
                "Sample title",
                "sample description for mapper unit testing",
                Status.IN_PROGRESS,
                LocalDateTime.now().plusDays(1)
            );
        Task entity = taskMapper.toEntity(taskRequest);

        assertThat(entity.getTitle()).isEqualTo(taskRequest.title());
        assertThat(entity.getDescription()).isEqualTo(taskRequest.description());
        assertThat(entity.getStatus()).isEqualTo(taskRequest.status());
        assertThat(entity.getDueDate()).isEqualTo(taskRequest.dueDate());
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Task entity =
            Task.builder()
                .id(1L)
                .title("Sample Title")
                .description("Sample description for unit testing mapper class")
                .status(Status.COMPLETED)
                .dueDate(LocalDateTime.of(2025, 8, 1, 12, 0))
                .build();

        TaskResponse response = taskMapper.toDto(entity);

        assertThat(response.id()).isEqualTo(entity.getId());
        assertThat(response.title()).isEqualTo(entity.getTitle());
        assertThat(response.description()).isEqualTo(entity.getDescription());
        assertThat(response.status()).isEqualTo(entity.getStatus());
        assertThat(response.dueDate()).isEqualTo(entity.getDueDate());
    }
}
