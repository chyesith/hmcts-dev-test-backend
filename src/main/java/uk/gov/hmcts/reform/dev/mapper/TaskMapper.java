package uk.gov.hmcts.reform.dev.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Task;

@Component
public class TaskMapper {

    public Task toEntity(TaskRequest taskRequest) {
        return Task.builder()
            .title(taskRequest.title())
            .description(taskRequest.description())
            .status(taskRequest.status())
            .dueDate(taskRequest.dueDate())
            .build();
    }

    public TaskResponse toDto(Task currentTask) {
        return new TaskResponse(
            currentTask.getId(),
            currentTask.getTitle(),
            currentTask.getDescription(),
            currentTask.getStatus(),
            currentTask.getDueDate(),
            currentTask.getCreatedAt(),
            currentTask.getUpdatedAt());
    }
}
