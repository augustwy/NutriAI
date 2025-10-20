package com.nexon.nutriai.repository.entity;

import jakarta.persistence.*;
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
    private String phone;

    @Column(name = "request_id")
    private String requestId;

    @Lob
    @Column
    private String question;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column
    private String answer;

    @Column(name = "create_time")
    private Date createTime;

    public DialogueLog() {
        this.createTime = new Date();
    }
}
