package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.DialogueLog;

import java.util.Optional;

public interface DialogueLogPort {
    DialogueLog save(DialogueLog dialogueLog);

    Optional<DialogueLog> findById(Long dialogueLogId);
}
