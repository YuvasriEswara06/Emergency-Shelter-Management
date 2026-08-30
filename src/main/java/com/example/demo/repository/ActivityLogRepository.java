package com.example.demo.repository;

import com.example.demo.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    List<ActivityLog> findByUserUserId(Integer userId);

    List<ActivityLog> findByAffectedTable(String affectedTable);
}