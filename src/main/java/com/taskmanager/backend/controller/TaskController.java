package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.MessageResponse;
import com.taskmanager.backend.dto.TaskRequest;
import com.taskmanager.backend.dto.TaskResponse;
import com.taskmanager.backend.exception.AccessDeniedException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.model.Task;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.service.FileStorageService;
import com.taskmanager.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

        private final TaskService taskService;
        private final FileStorageService fileStorageService;
        private final TaskRepository taskRepository;

        @Operation(summary = "Create a new task", description = "Creates a new task for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @PostMapping
        public ResponseEntity<TaskResponse> createTask(
                        @Valid @RequestBody TaskRequest taskRequest,
                        Authentication authentication) {
                TaskResponse response = taskService.createTask(taskRequest, authentication.getName());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Get all tasks", description = "Returns paginated list of tasks for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @GetMapping
        public ResponseEntity<Page<TaskResponse>> getAllTasks(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
                        @Parameter(description = "Filter by status") @RequestParam(required = false) Task.Status status,
                        @Parameter(description = "Filter by priority") @RequestParam(required = false) Task.Priority priority,
                        Authentication authentication) {

                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<TaskResponse> tasks = taskService.getAllTasks(
                                authentication.getName(), status, priority, pageable);
                return ResponseEntity.ok(tasks);
        }

        @Operation(summary = "Get task by ID", description = "Returns a specific task by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task found", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping("/{id}")
        public ResponseEntity<TaskResponse> getTaskById(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        Authentication authentication) {
                TaskResponse response = taskService.getTaskById(id, authentication.getName());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Update task", description = "Updates an existing task")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PutMapping("/{id}")
        public ResponseEntity<TaskResponse> updateTask(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        @Valid @RequestBody TaskRequest taskRequest,
                        Authentication authentication) {
                TaskResponse response = taskService.updateTask(id, taskRequest, authentication.getName());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete task", description = "Soft deletes a task (preserves data for auditing)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Task not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteTask(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        Authentication authentication) {
                taskService.deleteTask(id, authentication.getName());
                return ResponseEntity.noContent().build();
        }

        // ==================== FILE UPLOAD ENDPOINTS ====================

        @Operation(summary = "Upload attachment", description = "Upload a file attachment to a task")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file"),
                        @ApiResponse(responseCode = "404", description = "Task not found")
        })
        @PostMapping(value = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<MessageResponse> uploadAttachment(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
                        Authentication authentication) {

                Task task = getTaskAndValidateOwnership(id, authentication.getName());

                // Delete old attachment if exists
                if (task.getAttachmentPath() != null) {
                        fileStorageService.deleteFile(task.getAttachmentPath());
                }

                String fileName = fileStorageService.storeFile(file, id);
                task.setAttachmentPath(fileName);
                taskRepository.save(task);

                return ResponseEntity.ok(new MessageResponse("File uploaded successfully: " + fileName));
        }

        @Operation(summary = "Download attachment", description = "Download the file attachment from a task")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
                        @ApiResponse(responseCode = "404", description = "Task or file not found")
        })
        @GetMapping("/{id}/attachment")
        public ResponseEntity<Resource> downloadAttachment(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        Authentication authentication) {

                Task task = getTaskAndValidateOwnership(id, authentication.getName());

                if (task.getAttachmentPath() == null) {
                        throw new ResourceNotFoundException("Attachment", "taskId", id);
                }

                Resource resource = fileStorageService.loadFileAsResource(task.getAttachmentPath());

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + task.getAttachmentPath() + "\"")
                                .body(resource);
        }

        @Operation(summary = "Delete attachment", description = "Remove the file attachment from a task")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Task not found")
        })
        @DeleteMapping("/{id}/attachment")
        public ResponseEntity<MessageResponse> deleteAttachment(
                        @Parameter(description = "Task ID") @PathVariable Long id,
                        Authentication authentication) {

                Task task = getTaskAndValidateOwnership(id, authentication.getName());

                if (task.getAttachmentPath() != null) {
                        fileStorageService.deleteFile(task.getAttachmentPath());
                        task.setAttachmentPath(null);
                        taskRepository.save(task);
                }

                return ResponseEntity.ok(new MessageResponse("Attachment deleted successfully"));
        }

        private Task getTaskAndValidateOwnership(Long id, String username) {
                Task task = taskRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

                if (!task.getUser().getUsername().equals(username)) {
                        throw new AccessDeniedException("You don't have permission to access this task");
                }

                return task;
        }
}
