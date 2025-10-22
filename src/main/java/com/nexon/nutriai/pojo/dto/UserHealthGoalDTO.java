package com.nexon.nutriai.pojo.dto;

import com.nexon.nutriai.repository.entity.UserHealthGoal;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserHealthGoalDTO {
    private String phone;

    private double weight;

    private String bfr;

    private String goal;

    private Date updateTime;

    public UserHealthGoal toEntity() {
        UserHealthGoal newUserHealthGoal = new UserHealthGoal();
        newUserHealthGoal.setPhone(phone);
        newUserHealthGoal.setWeight(weight);
        newUserHealthGoal.setBfr(bfr);
        newUserHealthGoal.setGoal(goal);
        newUserHealthGoal.setUpdateTime(updateTime);
        return newUserHealthGoal;
    }
}
