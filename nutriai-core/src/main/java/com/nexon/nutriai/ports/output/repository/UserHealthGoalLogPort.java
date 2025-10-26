package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.UserHealthGoalLog;

public interface UserHealthGoalLogPort {
    void save(UserHealthGoalLog userHealthGoalLog);
}
