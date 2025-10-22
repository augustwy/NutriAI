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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/web/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册接口
     */
    @PostMapping("/signUp")
    public BaseResponse<Object> register(
            @RequestParam String phone,
            @RequestParam String name,
            @RequestParam String password) {

        userService.signUp(phone, name, password);

        return new BaseResponse<>(ErrorCode.SUCCESS, "注册成功");
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/signIn")
    public BaseResponse<UserInfo> signIn(@RequestParam String phone, @RequestParam String password, HttpServletResponse response) {

        AppUser user = userService.signIn(phone, password);
        if (user == null) {
            return new BaseResponse<>(ErrorCode.SIGN_IN_ERROR);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(user.getPhone());
        userInfo.setUsername(user.getUsername());

        Map<String, String> tokens = jwtUtil.generateToken(user.getPhone());
        setToken(response, tokens);
        return new BaseResponse<>(userInfo);
    }

    /**
     * 刷新 Token 接口
     */
    @PostMapping("/refresh")
    public BaseResponse<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {

        // 1. 验证 Refresh Token 是否有效
        if (refreshToken != null && jwtUtil.validateToken(refreshToken) && jwtUtil.isRefreshToken(refreshToken)) {
            String phone = jwtUtil.getSubjectFromToken(refreshToken);
            // 2. 生成新的双 Token
            Map<String, String> newTokens = jwtUtil.generateToken(phone);

            setToken(response, newTokens);
            return BaseResponse.success();
        }
        return new BaseResponse<>(ErrorCode.TOKEN_REFRESH_ERROR);
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public BaseResponse<?> logout(@RequestParam String phone) {
        log.info("logout: {}", phone);

        return new BaseResponse<>(ErrorCode.SUCCESS, "退出成功");
    }

    /**
     * 更新用户信息
     * @param userProfileDTO
     * @return
     */
    @PostMapping("/updateUserProfile")
    public BaseResponse<?> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
        String phone = ThreadLocalUtil.getPhone();

        userProfileDTO.setPhone(phone);
        userService.updateUserProfile(userProfileDTO);
        return new BaseResponse<>(ErrorCode.SUCCESS, "更新成功");
    }

    /**
     * 获取用户信息
     * @param phone
     * @return
     */
    @PostMapping("/getUserProfile")
    public BaseResponse<UserProfile> getUserProfile(@RequestParam String phone) {
        UserProfile userProfile = userService.getUserProfile(phone);
        return new BaseResponse<>(userProfile);
    }

    private void setToken(HttpServletResponse response, Map<String, String> tokens) {
        response.setHeader("Authorization", "Bearer " + tokens.get("accessToken"));

        // 创建 HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true) // 关键！JS 无法读取
                .secure(true)   // 关键！只能在 HTTPS 下传输
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7天
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
