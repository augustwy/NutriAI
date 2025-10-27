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
@Table(name = "user_profile_log", indexes = @Index(name = "phone_user_profile_log_idx", columnList = "phone"))
public class UserProfileLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String phone;

    @Column
    private double height;

    @Column
    private double weight;

    @Column
    private int age;

    @Column
    private char gender;

    @Column(length = 10)
    private double bmi;

    @Column(length = 10)
    private String bfr;

    @Column(name = "eating_habits", length = 500)
    private String eatingHabits;

    @Column(name = "create_time")
    private Date createTime;


}
