package com.nexon.nutriai.pojo;

import com.nexon.nutriai.repository.entity.UserProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserProfileDTO {

    private String phone;

    private double height;

    private double weight;

    private int age;

    private char gender;

    private double bmi;

    private String bfr;

    private String eatingHabits;

    private Date updateTime;

    public UserProfile toEntity() {
        UserProfile newUserProfile = new UserProfile();
        newUserProfile.setPhone(phone);
        newUserProfile.setHeight(height);
        newUserProfile.setWeight(weight);
        newUserProfile.setAge(age);
        newUserProfile.setGender(gender);
        newUserProfile.setBmi(bmi);
        newUserProfile.setBfr(bfr);
        newUserProfile.setEatingHabits(eatingHabits);
        newUserProfile.setUpdateTime(updateTime);
        return newUserProfile;
    }
}
