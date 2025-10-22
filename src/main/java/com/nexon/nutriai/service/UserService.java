package com.nexon.nutriai.service;

import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.dto.UserHealthGoalDTO;
import com.nexon.nutriai.pojo.dto.UserProfileDTO;
import com.nexon.nutriai.repository.UserHealthGoalLogRepository;
import com.nexon.nutriai.repository.UserHealthGoalRepository;
import com.nexon.nutriai.repository.UserProfileLogRepository;
import com.nexon.nutriai.repository.UserProfileRepository;
import com.nexon.nutriai.repository.UserRepository;
import com.nexon.nutriai.repository.entity.AppUser;
import com.nexon.nutriai.repository.entity.UserHealthGoal;
import com.nexon.nutriai.repository.entity.UserHealthGoalLog;
import com.nexon.nutriai.repository.entity.UserProfile;
import com.nexon.nutriai.repository.entity.UserProfileLog;
import com.nexon.nutriai.util.PasswordUtil;
import com.nexon.nutriai.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileLogRepository userProfileLogRepository;
    private final UserHealthGoalRepository userHealthGoalRepository;
    private final UserHealthGoalLogRepository userHealthGoalLogRepository;

    public void signUp(String phone, String name, String password) {
        log.info("Sign up: {}, {}", phone, name);
        Optional<AppUser> optional = userRepository.findById(phone);
        if (optional.isPresent()) {
            throw new NutriaiException(ErrorCode.SIGN_UP_ERROR, "手机号码已存在");
        }

        String salt = UUIDUtil.generateShortUUID(16);
        String hashPassword = PasswordUtil.hashPassword(password, salt);

        AppUser appUser = new AppUser();
        appUser.setPhone(phone);
        appUser.setUsername(name);
        appUser.setPassword(hashPassword);
        userRepository.save(appUser);

        log.info("signUp success: {}", appUser.getPhone());
    }

    public AppUser signIn(String phone, String password) {
        log.info("signIn: {}", phone);
        Optional<AppUser> optional = userRepository.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }

        AppUser appUser = optional.get();
        if (!PasswordUtil.verifyPassword(password, appUser.getPassword())) {
            return null;
        }
        log.info("signIn success: {}", appUser.getPhone());
        return appUser;
    }

    @Transactional
    public void updateUserProfile(UserProfileDTO userProfileDTO) {
        log.info("updateUserProfile: {}", JSONObject.toJSONString(userProfileDTO));

        UserProfile userProfile = userProfileDTO.toEntity();
        userProfile.setUpdateTime(new Date());
        double bmi = calculateBMI(userProfile.getHeight(), userProfile.getWeight());
        userProfile.setBmi(bmi);
        // 插入或更新
        userProfileRepository.save(userProfile);

        UserProfileLog userProfileLog = buildUserProfileLog(userProfile);
        userProfileLog.setCreateTime(userProfile.getUpdateTime());
        userProfileLogRepository.save(userProfileLog);

        log.info("updateUserProfile success: {}", JSONObject.toJSONString(userProfile));
    }

    public UserProfile getUserProfile(String phone) {
        log.info("getUserProfile: {}", phone);
        Optional<UserProfile> optional = userProfileRepository.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }
        log.info("getUserProfile success: {}", JSONObject.toJSONString(optional.get()));
        return optional.get();
    }

    @Transactional
    public void updateUserHealthGoal(UserHealthGoalDTO userHealthGoalDTO) {
        log.info("updateUserHealthGoal: {}", JSONObject.toJSONString(userHealthGoalDTO));

        UserHealthGoal userHealthGoal = userHealthGoalDTO.toEntity();
        userHealthGoal.setUpdateTime(new Date());
        userHealthGoalRepository.save(userHealthGoal);

        UserHealthGoalLog userHealthGoalLog = buildUserHealthGoalLog(userHealthGoal);
        userHealthGoalLog.setCreateTime(userHealthGoal.getUpdateTime());
        userHealthGoalLogRepository.save(userHealthGoalLog);
    }

    public UserHealthGoal getUserHealthGoal(String phone) {
        log.info("getUserHealthGoal: {}", phone);
        Optional<UserHealthGoal> optional = userHealthGoalRepository.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }
        log.info("getUserHealthGoal success: {}", JSONObject.toJSONString(optional.get()));
        return optional.get();
    }

    private UserProfileLog buildUserProfileLog(UserProfile userProfile) {
        UserProfileLog userProfileLog = new UserProfileLog();
        userProfileLog.setPhone(userProfile.getPhone());
        userProfileLog.setHeight(userProfile.getHeight());
        userProfileLog.setWeight(userProfile.getWeight());
        userProfileLog.setAge(userProfile.getAge());
        userProfileLog.setGender(userProfile.getGender());
        userProfileLog.setBmi(userProfile.getBmi());
        userProfileLog.setBfr(userProfile.getBfr());
        userProfileLog.setEatingHabits(userProfile.getEatingHabits());
        return userProfileLog;
    }

    private UserHealthGoalLog buildUserHealthGoalLog(UserHealthGoal userHealthGoal) {
        UserHealthGoalLog userHealthGoalLog = new UserHealthGoalLog();
        userHealthGoalLog.setPhone(userHealthGoal.getPhone());
        userHealthGoalLog.setWeight(userHealthGoal.getWeight());
        userHealthGoalLog.setBfr(userHealthGoal.getBfr());
        return userHealthGoalLog;
    }

    /**
     * 计算BMI(简单版)
     * @param height
     * @param weight
     * @return
     */
    private double calculateBMI(double height, double weight) {
        if (height <= 0 || weight <= 0 || height > 300 || weight > 300) {
            return 0;
        }
        height = height / 100;
        double bmi = weight / (height * height);
        return Math.round(bmi * 100.0) / 100.0;
    }
}
