package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.BaseResponse;
import com.nexon.nutriai.pojo.UserInfo;
import com.nexon.nutriai.pojo.UserProfileDTO;
import com.nexon.nutriai.repository.entity.AppUser;
import com.nexon.nutriai.repository.entity.UserProfile;
import com.nexon.nutriai.service.UserService;
import com.nexon.nutriai.util.JwtUtil;
import com.nexon.nutriai.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/web/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/signUp")
    public ResponseEntity<BaseResponse<Object>> register(
            @RequestParam String phone,
            @RequestParam String name,
            @RequestParam String password) {

        userService.signUp(phone, name, password);

        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SUCCESS, "注册成功"));
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/signIn")
    public ResponseEntity<BaseResponse<UserInfo>> signIn(@RequestParam String phone, @RequestParam String password, HttpServletResponse response) {

        AppUser user = userService.signIn(phone, password);
        if (user == null) {
            return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SIGN_IN_ERROR));
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(user.getPhone());
        userInfo.setUsername(user.getUsername());

        String token = jwtUtil.generateToken(user.getPhone());
        response.setHeader("Authorization", "Bearer " + token);
        return ResponseEntity.ok(new BaseResponse<>(userInfo));
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Object>> logout(@RequestParam String phone) {
        log.info("logout: {}", phone);

        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SUCCESS, "退出成功"));
    }

    /**
     * 更新用户信息
     * @param userProfileDTO
     * @return
     */
    @PostMapping("/updateUserProfile")
    public ResponseEntity<BaseResponse<Object>> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
        String phone = ThreadLocalUtil.getPhone();

        userProfileDTO.setPhone(phone);
        userService.updateUserProfile(userProfileDTO);
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SUCCESS, "更新成功"));
    }

    /**
     * 获取用户信息
     * @param phone
     * @return
     */
    @PostMapping("/getUserProfile")
    public ResponseEntity<BaseResponse<Object>> getUserProfile(@RequestParam String phone) {
        UserProfile userProfile = userService.getUserProfile(phone);
        return ResponseEntity.ok(new BaseResponse<>(userProfile));
    }
}
