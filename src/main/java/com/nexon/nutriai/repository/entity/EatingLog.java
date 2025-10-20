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
@Table(name = "eating_log", indexes = @Index(name = "phone_eating_log_idx", columnList = "phone"))
public class EatingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long phone;

    @Column(name = "eat_time")
    private String eatTime;

    @Column
    private double calorie;

    @Column
    private String food;

    @Column(name = "create_time")
    private Date createTime;
}
