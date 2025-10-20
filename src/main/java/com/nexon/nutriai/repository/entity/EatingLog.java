package com.nexon.nutriai.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "eating_log", indexes = @Index(name = "phone_eating_log_idx", columnList = "phone"))
public class EatingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String phone;

    @Column(name = "eat_time")
    private String eatTime;

    @Column
    private double calorie;

    @Lob
    @Column
    private String food;

    @Column(name = "create_time")
    private Date createTime;

    public EatingLog() {
        this.createTime = new Date();
    }
}
