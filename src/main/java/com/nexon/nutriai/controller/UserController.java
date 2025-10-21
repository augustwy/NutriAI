package com.nexon.nutriai.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.BaseResponse;
import com.nexon.nutriai.pojo.UserInfo;
import com.nexon.nutriai.pojo.UserProfileDTO;
import com.nexon.nutriai.repository.entity.AppUser;
import com.nexon.nutriai.service.UserService;
import com.nexon.nutriai.util.SmCryptoUtil;
import com.nexon.nutriai.util.ThreadLocalUtil;
import com.nexon.nutriai.util.UUIDUtil;
import com.nexon.nutriai.util.cache.Cache;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/user")
public class UserController {

    private final UserService userService;
    private final Cache<String, UserInfo> userCache;
    public UserController(UserService userService, Cache<String, UserInfo> inMemoryCache) {
        this.userService = userService;

        this.userCache = inMemoryCache;
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
    public ResponseEntity<BaseResponse<UserInfo>> signIn(@RequestParam String phone, @RequestParam String password, HttpSession session) {

        AppUser user = userService.signIn(phone, password);
        if (user == null) {
            return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SIGN_IN_ERROR));
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(user.getPhone());
        userInfo.setUsername(user.getUsername());

        String token = SmCryptoUtil.sm3Hash(user.getPhone() + "#" + user.getUsername());
        userInfo.setToken(token);

        userCache.put(token, userInfo, 60 * 60 * 1000);
        // 登录成功，将用户信息存储到会话中
        session.setAttribute("currentUser", JSONObject.toJSONString(userInfo));
        return ResponseEntity.ok(new BaseResponse<>(userInfo));
    }

    /**
     * 用户登出接口
     */
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Object>> logout(@RequestParam String phone, HttpSession session) {
        // 清除会话中的用户信息
        session.removeAttribute("currentUser");
        session.invalidate();

        // 清除缓存
        userCache.remove(phone);
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SUCCESS, "注册成功"));
    }

    @PostMapping("/updateUserProfile")
    public ResponseEntity<BaseResponse<Object>> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO) {
        String phone = ThreadLocalUtil.getPhone();

        userProfileDTO.setPhone(phone);
        userService.updateUserProfile(userProfileDTO);
        return ResponseEntity.ok(new BaseResponse<>(ErrorCode.SUCCESS, "更新成功"));
    }
}
