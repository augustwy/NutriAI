package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.UserHealthGoalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthGoalLogRepository extends JpaRepository<UserHealthGoalLog, Long> {
}
