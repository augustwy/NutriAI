package com.nexon.nutriai.repository.entity;

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
@Table(name = "dialogue_session")
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
