package com.nexon.nutriai.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user_health_goal")
public class UserHealthGoal {

    @Id
    private String phone;

    @Column
    private double weight;

    @Column(length = 10)
    private String bfr;

    @Column(name = "health_goal", length = 10)
    private String healthGoal;

    @Column(name = "update_time")
    private Date updateTime;
}
