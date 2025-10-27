package com.nexon.nutriai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dialogue_detail", 
       indexes = {
           @Index(name = "session_id_dialogue_detail_idx", columnList = "session_id"),
           @Index(name = "session_id_sequence_idx", columnList = "session_id,sequence")
       })
public class DialogueDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "message_type", nullable = false)
    private Integer messageType; // 1-用户消息，2-AI回复

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}