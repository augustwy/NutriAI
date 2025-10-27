package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.SingleRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SingleRequestLogRepository extends JpaRepository<SingleRequestLog, Long> {
}
