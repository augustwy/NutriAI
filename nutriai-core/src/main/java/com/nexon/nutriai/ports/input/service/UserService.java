package com.nexon.nutriai.ports.input.service;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.domain.entity.*;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.ports.output.repository.*;
import com.nexon.nutriai.utils.PasswordUtil;
import com.nexon.nutriai.utils.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserPort userPort;
    private final UserProfilePort userProfilePort;
    private final UserProfileLogPort userProfileLogPort;
    private final UserHealthGoalPort userHealthGoalPort;
    private final UserHealthGoalLogPort userHealthGoalLogPort;

    public void signUp(String phone, String name, String password) {
        Optional<AppUser> optional = userPort.findById(phone);
        if (optional.isPresent()) {
            throw new NutriaiException(ErrorCode.SIGN_UP_ERROR, "手机号码已存在");
        }

        String salt = UUIDUtil.generateShortUUID(16);
        String hashPassword = PasswordUtil.hashPassword(password, salt);

        AppUser appUser = new AppUser();
        appUser.setPhone(phone);
        appUser.setUsername(name);
        appUser.setPassword(hashPassword);
        userPort.save(appUser);
    }

    /**
     * 登录
     *
     * @param phone
     * @param password
     * @return
     */
    public AppUser signIn(String phone, String password) {
        Optional<AppUser> optional = userPort.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }

        AppUser appUser = optional.get();
        if (!PasswordUtil.verifyPassword(password, appUser.getPassword())) {
            return null;
        }

        // 用户登录后缓存用户信息
//        getUserInformation(phone);
        return appUser;
    }

    public AppUser signInWithOpenId(String phone, String openId) {
        Optional<AppUser> optional = userPort.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }
        AppUser appUser = optional.get();
        // 用户登录后缓存用户信息
//        getUserInformation(phone);
        return appUser;
    }

    /**
     * 更新用户基础信息
     *
     * @param userProfile
     */
    public void updateUserProfile(UserProfile userProfile) {

        userProfile.setUpdateTime(new Date());
        double bmi = calculateBMI(userProfile.getHeight(), userProfile.getWeight());
        userProfile.setBmi(bmi);
        // 插入或更新
        userProfilePort.save(userProfile);

        UserProfileLog userProfileLog = buildUserProfileLog(userProfile);
        userProfileLog.setCreateTime(userProfile.getUpdateTime());
        userProfileLogPort.save(userProfileLog);

        // 用户信息更新时，获取用户信息
//        updateUserInformation(userProfileDTO.getPhone(), null, userProfile, null);

    }

    /**
     * 获取用户基础信息
     *
     * @param phone
     * @return
     */
    public UserProfile getUserProfile(String phone) {
        Optional<UserProfile> optional = userProfilePort.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }
        return optional.get();
    }

    /**
     * 更新用户健康目标
     *
     * @param userHealthGoal
     */
    public void updateUserHealthGoal(UserHealthGoal userHealthGoal) {

        userHealthGoal.setUpdateTime(new Date());
        userHealthGoalPort.save(userHealthGoal);

        UserHealthGoalLog userHealthGoalLog = buildUserHealthGoalLog(userHealthGoal);
        userHealthGoalLog.setCreateTime(userHealthGoal.getUpdateTime());
        userHealthGoalLogPort.save(userHealthGoalLog);

        // 用户信息更新时，获取用户信息
//        updateUserInformation(userHealthGoalDTO.getPhone(), null, null, userHealthGoal);
    }

    /**
     * 获取用户健康目标
     *
     * @param phone
     * @return
     */
    public UserHealthGoal getUserHealthGoal(String phone) {
        Optional<UserHealthGoal> optional = userHealthGoalPort.findById(phone);
        if (optional.isEmpty()) {
            return null;
        }
        return optional.get();
    }

//    /**
//     * 获取用户信息
//     *
//     * @param phone
//     * @return
//     */
//    public UserInformationDTO getUserInformation(String phone) {
//        UserInformationDTO userInformationDTO = userCache.get(phone);
//        if (userInformationDTO != null) {
//            return userInformationDTO;
//        }
//
//        Optional<AppUser> optional = userPort.findById(phone);
//        if (optional.isEmpty()) {
//            return null;
//        }
//
//        AppUser appUser = optional.get();
//        UserProfile userProfile = getUserProfile(phone);
//        UserHealthGoal userHealthGoal = getUserHealthGoal(phone);
//        userInformationDTO = new UserInformationDTO(appUser, userProfile, userHealthGoal);
//        userCache.put(phone, userInformationDTO);
//        return userInformationDTO;
//    }

    public AppUser findByOpenId(String openId) {
        if (openId == null) {
            return null;
        }
        return userPort.findByOpenId(openId);
    }

//    /**
//     * 更新用户信息缓存
//     *
//     * @param phone
//     * @param appUser
//     * @param userProfile
//     * @param userHealthGoal
//     */
//    private void updateUserInformation(String phone, AppUser appUser, UserProfile userProfile, UserHealthGoal userHealthGoal) {
//        UserInformationDTO userInformationDTO = userCache.get(phone);
//        if (userInformationDTO != null) {
//            // 按需更新缓存
//            userCache.put(phone, new UserInformationDTO(appUser != null ? appUser : userInformationDTO.appUser(),
//                    userProfile != null ? userProfile : userInformationDTO.userProfile(),
//                    userHealthGoal != null ? userHealthGoal : userInformationDTO.userHealthGoal()));
//        } else {
//            // 没有缓存则全量添加
//            getUserInformation(phone);
//        }
//    }


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
     *
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

    public boolean updateOpenId(String phone, String openId) {
        Optional<AppUser> optional = userPort.findById(phone);
        if (optional.isEmpty()) {
            return false;
        }
        AppUser appUser = optional.get();
        appUser.setOpenId(openId);
        userPort.save(appUser);
        return true;
    }
}
