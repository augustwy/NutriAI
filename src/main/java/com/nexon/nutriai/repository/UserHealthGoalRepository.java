package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.UserHealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthGoalRepository extends JpaRepository<UserHealthGoal, String> {
}
