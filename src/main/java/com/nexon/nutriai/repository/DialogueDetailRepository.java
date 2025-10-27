package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.DialogueDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialogueDetailRepository extends JpaRepository<DialogueDetail, Long> {
    List<DialogueDetail> findBySessionIdOrderBySequence(String sessionId);
}
