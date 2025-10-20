package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.UserProfileLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileLogRepository extends JpaRepository<UserProfileLog, Long> {
}
