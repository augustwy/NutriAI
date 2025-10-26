package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.DialogueLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DialogueLogRepository extends JpaRepository<DialogueLog, Long> {
}
