package com.nexon.nutriai.repository.entity;

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
    private Long phone;

    @Column
    private double height;

    @Column
    private double weight;

    @Column
    private int age;

    @Column
    private char gender;

    @Column
    private double bmi;

    @Column
    private String bfr;

    @Column(name = "eating_habits")
    private String eatingHabits;

    @Column(name = "update_time")
    private Date updateTime;


}
