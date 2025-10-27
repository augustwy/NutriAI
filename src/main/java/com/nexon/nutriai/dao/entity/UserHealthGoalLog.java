package com.nexon.nutriai.dao.entity;

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
@Table(name = "user_health_goal_log", indexes = @Index(name = "phone_user_health_goal_log_idx", columnList = "phone"))
public class UserHealthGoalLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String phone;

    @Column
    private double weight;

    @Column(length = 10)
    private String bfr;

    @Column(name = "create_time")
    private Date createTime;
}
