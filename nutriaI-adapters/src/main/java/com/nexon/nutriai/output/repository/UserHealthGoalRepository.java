package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.UserHealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthGoalRepository extends JpaRepository<UserHealthGoal, String> {
}
