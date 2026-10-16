package com.taskflow.dto;

import java.util.Map;

/**
 * ProjectSummaryResponse — DTO para GET /projects/{id}/summary.
 */
public record ProjectSummaryResponse(
        Long projectId,
        String projectName,
        int totalTasks,
        Map<String, Integer> byStatus,
        int overdue
) {
}
