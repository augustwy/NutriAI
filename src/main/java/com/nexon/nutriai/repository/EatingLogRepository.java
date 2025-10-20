package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.EatingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EatingLogRepository extends JpaRepository<EatingLog, Long> {
}
