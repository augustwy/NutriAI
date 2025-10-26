package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.UserHealthGoal;

import java.util.Optional;

public interface UserHealthGoalPort {
    void save(UserHealthGoal userHealthGoal);

    Optional<UserHealthGoal> findById(String phone);
}
