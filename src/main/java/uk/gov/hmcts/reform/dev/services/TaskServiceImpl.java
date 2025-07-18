package uk.gov.hmcts.reform.dev.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dtos.TaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.mapper.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Status;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        if (taskRequest == null) {
            throw new NullPointerException("Task request is null valued");
        }
        return taskMapper.toDto(taskRepository.save(taskMapper.toEntity(taskRequest)));
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        return taskMapper.toDto(
            taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id)));
    }

    @Override
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream().map(taskMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTaskStatusById(Long id, Status status) {
        Task taskToUpdate =
            taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskToUpdate.setStatus(status);
        return taskMapper.toDto(taskRepository.save(taskToUpdate));
    }

    @Override
    public void deleteTask(Long id) {
        Task taskToDelete =
            taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(taskToDelete);
    }
}
