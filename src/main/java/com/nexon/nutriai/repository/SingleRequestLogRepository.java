package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.SingleRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleRequestLogRepository extends JpaRepository<SingleRequestLog, Long> {
}
