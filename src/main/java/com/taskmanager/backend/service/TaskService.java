package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.TaskRequest;
import com.taskmanager.backend.dto.TaskResponse;
import com.taskmanager.backend.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(TaskRequest taskRequest, String username);

    Page<TaskResponse> getAllTasks(String username, Task.Status status, Task.Priority priority, Pageable pageable);

    TaskResponse getTaskById(Long id, String username);

    TaskResponse updateTask(Long id, TaskRequest taskRequest, String username);

    void deleteTask(Long id, String username);
}
