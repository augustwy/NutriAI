package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.EatingLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EatingLogRepository extends JpaRepository<EatingLog, Long> {
}
