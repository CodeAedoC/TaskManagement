package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.TaskRequest;
import com.taskmanager.backend.dto.TaskResponse;
import com.taskmanager.backend.exception.AccessDeniedException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.model.Task;
import com.taskmanager.backend.model.User;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    @CacheEvict(value = { "tasks", "analytics" }, allEntries = true)
    public TaskResponse createTask(TaskRequest taskRequest, String username) {
        log.debug("Creating task for user: {} - cache evicted", username);
        User user = getUserByUsername(username);

        Task task = Task.builder()
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .priority(taskRequest.getPriority() != null ? taskRequest.getPriority() : Task.Priority.MEDIUM)
                .status(taskRequest.getStatus() != null ? taskRequest.getStatus() : Task.Status.PENDING)
                .dueDate(taskRequest.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);

        // --- INTEGRATION POINT: Email Notification ---
        emailService.sendTaskAssignmentEmail(
                user.getEmail(),
                savedTask.getTitle(),
                savedTask.getDueDate() != null ? savedTask.getDueDate().toString() : null);

        return mapToResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#username + '_' + #status + '_' + #priority + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<TaskResponse> getAllTasks(String username, Task.Status status, Task.Priority priority,
            Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Task> tasks = taskRepository.findTasksWithFilters(user, status, priority, pageable);
        return tasks.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskById", key = "#id + '_' + #username")
    public TaskResponse getTaskById(Long id, String username) {
        log.debug("Fetching task {} from database - result will be cached", id);
        Task task = getTaskAndValidateOwnership(id, username);
        return mapToResponse(task);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskById", key = "#id + '_' + #username"),
            @CacheEvict(value = "analytics", allEntries = true)
    })
    public TaskResponse updateTask(Long id, TaskRequest taskRequest, String username) {
        log.debug("Updating task {} - cache evicted", id);
        Task task = getTaskAndValidateOwnership(id, username);

        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        if (taskRequest.getPriority() != null) {
            task.setPriority(taskRequest.getPriority());
        }
        if (taskRequest.getStatus() != null) {
            task.setStatus(taskRequest.getStatus());
        }
        task.setDueDate(taskRequest.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tasks", allEntries = true),
            @CacheEvict(value = "taskById", key = "#id + '_' + #username"),
            @CacheEvict(value = "analytics", allEntries = true)
    })
    public void deleteTask(Long id, String username) {
        log.debug("Deleting task {} - cache evicted", id);
        Task task = getTaskAndValidateOwnership(id, username);
        // Soft delete
        task.setIsDeleted(true);
        taskRepository.save(task);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Task getTaskAndValidateOwnership(Long id, String username) {
        Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (!task.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You don't have permission to access this task");
        }

        return task;
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .username(task.getUser().getUsername())
                .attachmentPath(task.getAttachmentPath())
                .build();
    }
}
