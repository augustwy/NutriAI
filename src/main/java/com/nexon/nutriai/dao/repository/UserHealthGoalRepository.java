package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.UserHealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHealthGoalRepository extends JpaRepository<UserHealthGoal, String> {
}
