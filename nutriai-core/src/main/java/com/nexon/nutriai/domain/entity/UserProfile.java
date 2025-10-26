package com.nexon.nutriai.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user_profile")
public class UserProfile {
    @Id
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

    @Column(name = "update_time")
    private Date updateTime;


}
