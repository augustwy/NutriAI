package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.EatingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EatingLogRepository extends JpaRepository<EatingLog, Long> {
}
