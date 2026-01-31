package com.taskmanager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    // Summary statistics
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long cancelledTasks;
    private long overdueTasks;

    // Breakdown by priority
    private long lowPriorityTasks;
    private long mediumPriorityTasks;
    private long highPriorityTasks;
    private long criticalPriorityTasks;

    // Completion rate
    private double completionRate;

    // Tasks by status (for charts)
    private Map<String, Long> tasksByStatus;

    // Tasks by priority (for charts)
    private Map<String, Long> tasksByPriority;
}
