package uk.gov.hmcts.reform.dev.services;

import java.util.List;

import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.models.Status;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest);

    TaskResponse getTaskById(Long id);

    List<TaskResponse> getAllTasks();

    TaskResponse updateTaskStatusById(Long id, Status status);

    void deleteTask(Long id);
}
