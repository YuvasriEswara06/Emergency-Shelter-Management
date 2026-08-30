package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "affected_table", nullable = false, length = 50)
    private String affectedTable;

    @Column(name = "affected_id", nullable = false)
    private Integer affectedId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public ActivityLog() {
        // JPA
    }

    public ActivityLog(Users user, String action, String affectedTable, Integer affectedId) {
        this.user = user;
        this.action = action;
        this.affectedTable = affectedTable;
        this.affectedId = affectedId;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAffectedTable() {
        return affectedTable;
    }

    public void setAffectedTable(String affectedTable) {
        this.affectedTable = affectedTable;
    }

    public Integer getAffectedId() {
        return affectedId;
    }

    public void setAffectedId(Integer affectedId) {
        this.affectedId = affectedId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}