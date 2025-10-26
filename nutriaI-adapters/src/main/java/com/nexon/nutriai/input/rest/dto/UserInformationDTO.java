package com.nexon.nutriai.input.rest.dto;


import com.nexon.nutriai.domain.entity.AppUser;
import com.nexon.nutriai.domain.entity.UserHealthGoal;
import com.nexon.nutriai.domain.entity.UserProfile;

public record UserInformationDTO(AppUser appUser, UserProfile userProfile, UserHealthGoal userHealthGoal) {

    public String buildUserInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("用户信息：")
                .append("用户名：")
                .append(appUser.getUsername())
                .append("手机号码：")
                .append(appUser.getPhone());
        if (userProfile != null) {
            sb.append("身高：")
                    .append(userProfile.getHeight())
                    .append("体重：")
                    .append(userProfile.getWeight())
                    .append("年龄：")
                    .append(userProfile.getAge())
                    .append("性别：")
                    .append(userProfile.getGender())
                    .append("BMI：")
                    .append(userProfile.getBmi())
                    .append("BFR：")
                    .append(userProfile.getBfr())
                    .append("饮食习惯：")
                    .append(userProfile.getEatingHabits());
        }
        if (userHealthGoal != null) {
            sb.append("目标健康指数：")
                    .append(userHealthGoal.getBfr())
                    .append("目标健康指数：")
                    .append(userHealthGoal.getBfr())
                    .append("目标健康指数：")
                    .append(userHealthGoal.getBfr());
        }
        return sb.toString();
    }

}
