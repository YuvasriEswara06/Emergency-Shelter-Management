package com.example.demo.controller;

import com.example.demo.entity.ActivityLog;
import com.example.demo.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    // Helper to sanitize ActivityLog (avoid exposing Users.passwordHash)
    private Map<String, Object> sanitize(ActivityLog log) {
        return Map.of(
                "logId", log.getLogId(),
                "userId", log.getUser() == null ? null : log.getUser().getUserId(),
                "username", log.getUser() == null ? null : log.getUser().getUsername(),
                "action", log.getAction(),
                "affectedTable", log.getAffectedTable(),
                "affectedId", log.getAffectedId(),
                "timestamp", log.getTimestamp()
        );
    }

    // GET /api/activity-log or /api/activity-log?userId=123
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLogs(@RequestParam(required = false) Integer userId) {
        List<ActivityLog> logs;
        if (userId != null) {
            logs = activityLogService.getLogsByUser(userId);
        } else {
            logs = activityLogService.getAllLogs();
        }
        List<Map<String, Object>> result = logs.stream().map(this::sanitize).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // GET /api/activity-log/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Integer id) {
        return activityLogService.getLogById(id)
                .map(log -> ResponseEntity.ok(sanitize(log)))
                .orElse(ResponseEntity.notFound().build());
    }
}