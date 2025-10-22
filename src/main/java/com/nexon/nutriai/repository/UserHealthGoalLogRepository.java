package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.UserHealthGoalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthGoalLogRepository extends JpaRepository<UserHealthGoalLog, Long> {
}
