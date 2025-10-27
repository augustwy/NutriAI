package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.DialogueSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DialogueSessionRepository extends JpaRepository<DialogueSession, Long> {
    Optional<DialogueSession> findBySessionId(String sessionId);
}