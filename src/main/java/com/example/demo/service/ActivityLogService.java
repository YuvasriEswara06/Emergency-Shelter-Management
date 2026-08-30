package com.example.demo.service;

import com.example.demo.entity.ActivityLog;
import com.example.demo.entity.Users;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UsersRepository usersRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UsersRepository usersRepository) {
        this.activityLogRepository = activityLogRepository;
        this.usersRepository = usersRepository;
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }

    public Optional<ActivityLog> getLogById(Integer logId) {
        return activityLogRepository.findById(logId);
    }

    public ActivityLog createLog(ActivityLog log) {
        return activityLogRepository.save(log);
    }

    public ActivityLog logAction(Integer userId, String action, String affectedTable, Integer affectedId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        ActivityLog log = new ActivityLog(user, action, affectedTable, affectedId);
        return activityLogRepository.save(log);
    }

    public List<ActivityLog> getLogsByUser(Integer userId) {
        return activityLogRepository.findByUserUserId(userId);
    }
}