package com.nexon.nutriai.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "dialogue_log", indexes = {@Index(name = "phone_dialogue_log_idx", columnList = "phone"), @Index(name = "request_id_dialogue_log_idx", columnList = "request_id")})
public class DialogueLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long phone;

    @Column(name = "request_id")
    private String requestId;

    @Column
    private String question;

    @Column
    private String answer;

    @Column(name = "create_time")
    private Date createTime;
}
