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
@Table(name = "dialogue_session", 
       indexes = {
           @Index(name = "phone_dialogue_session_idx", columnList = "phone"),
           @Index(name = "phone_method_name_idx", columnList = "phone,methodName"),
           @Index(name = "session_id_idx", columnList = "sessionId")
       })
public class DialogueSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "method_name", length = 255)
    private String methodName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();
}