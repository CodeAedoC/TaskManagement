package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.AnalyticsResponse;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.model.Task;
import com.taskmanager.backend.model.User;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Task analytics and statistics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

        private final TaskRepository taskRepository;
        private final UserRepository userRepository;

        @Operation(summary = "Get task summary", description = "Returns comprehensive task statistics for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @GetMapping("/summary")
        @Cacheable(value = "analytics", key = "#authentication.name")
        public ResponseEntity<AnalyticsResponse> getAnalyticsSummary(Authentication authentication) {
                User user = getUserByUsername(authentication.getName());

                // Count by status
                long totalTasks = taskRepository.countByUserAndIsDeletedFalse(user);
                long completedTasks = taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.COMPLETED);
                long pendingTasks = taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.PENDING);
                long inProgressTasks = taskRepository.countByUserAndStatusAndIsDeletedFalse(user,
                                Task.Status.IN_PROGRESS);
                long cancelledTasks = taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.CANCELLED);
                long overdueTasks = taskRepository.countOverdueTasks(user, LocalDateTime.now(), Task.Status.COMPLETED);

                // Count by priority
                long lowPriority = taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.LOW);
                long mediumPriority = taskRepository.countByUserAndPriorityAndIsDeletedFalse(user,
                                Task.Priority.MEDIUM);
                long highPriority = taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.HIGH);
                long criticalPriority = taskRepository.countByUserAndPriorityAndIsDeletedFalse(user,
                                Task.Priority.CRITICAL);

                // Calculate completion rate
                double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

                // Build maps for charts
                Map<String, Long> tasksByStatus = new LinkedHashMap<>();
                tasksByStatus.put("PENDING", pendingTasks);
                tasksByStatus.put("IN_PROGRESS", inProgressTasks);
                tasksByStatus.put("COMPLETED", completedTasks);
                tasksByStatus.put("CANCELLED", cancelledTasks);

                Map<String, Long> tasksByPriority = new LinkedHashMap<>();
                tasksByPriority.put("LOW", lowPriority);
                tasksByPriority.put("MEDIUM", mediumPriority);
                tasksByPriority.put("HIGH", highPriority);
                tasksByPriority.put("CRITICAL", criticalPriority);

                AnalyticsResponse response = AnalyticsResponse.builder()
                                .totalTasks(totalTasks)
                                .completedTasks(completedTasks)
                                .pendingTasks(pendingTasks)
                                .inProgressTasks(inProgressTasks)
                                .cancelledTasks(cancelledTasks)
                                .overdueTasks(overdueTasks)
                                .lowPriorityTasks(lowPriority)
                                .mediumPriorityTasks(mediumPriority)
                                .highPriorityTasks(highPriority)
                                .criticalPriorityTasks(criticalPriority)
                                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                                .tasksByStatus(tasksByStatus)
                                .tasksByPriority(tasksByPriority)
                                .build();

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get tasks by status breakdown", description = "Returns task counts grouped by status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Status breakdown retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @GetMapping("/by-status")
        public ResponseEntity<Map<String, Long>> getTasksByStatus(Authentication authentication) {
                User user = getUserByUsername(authentication.getName());

                Map<String, Long> tasksByStatus = new LinkedHashMap<>();
                tasksByStatus.put("PENDING",
                                taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.PENDING));
                tasksByStatus.put("IN_PROGRESS",
                                taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.IN_PROGRESS));
                tasksByStatus.put("COMPLETED",
                                taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.COMPLETED));
                tasksByStatus.put("CANCELLED",
                                taskRepository.countByUserAndStatusAndIsDeletedFalse(user, Task.Status.CANCELLED));

                return ResponseEntity.ok(tasksByStatus);
        }

        @Operation(summary = "Get tasks by priority breakdown", description = "Returns task counts grouped by priority")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Priority breakdown retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        @GetMapping("/by-priority")
        public ResponseEntity<Map<String, Long>> getTasksByPriority(Authentication authentication) {
                User user = getUserByUsername(authentication.getName());

                Map<String, Long> tasksByPriority = new LinkedHashMap<>();
                tasksByPriority.put("LOW",
                                taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.LOW));
                tasksByPriority.put("MEDIUM",
                                taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.MEDIUM));
                tasksByPriority.put("HIGH",
                                taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.HIGH));
                tasksByPriority.put("CRITICAL",
                                taskRepository.countByUserAndPriorityAndIsDeletedFalse(user, Task.Priority.CRITICAL));

                return ResponseEntity.ok(tasksByPriority);
        }

        private User getUserByUsername(String username) {
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        }
}
