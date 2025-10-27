package com.nexon.nutriai.pojo.dto;

import com.nexon.nutriai.dao.entity.UserHealthGoal;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@Getter
@Setter
public class UserHealthGoalDTO {
    @NotNull
    private String phone;

    private double weight;

    private String bfr;

    private String healthGoal;

    private Date updateTime;

    public UserHealthGoal toEntity() {
        UserHealthGoal newUserHealthGoal = new UserHealthGoal();
        newUserHealthGoal.setPhone(phone);
        newUserHealthGoal.setWeight(weight);
        newUserHealthGoal.setBfr(bfr);
        newUserHealthGoal.setHealthGoal(healthGoal);
        newUserHealthGoal.setUpdateTime(updateTime);
        return newUserHealthGoal;
    }
}
