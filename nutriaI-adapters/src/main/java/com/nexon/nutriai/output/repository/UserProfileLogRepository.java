package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.UserProfileLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileLogRepository extends JpaRepository<UserProfileLog, Long> {
}
