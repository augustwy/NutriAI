package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.dto.UserHealthGoalDTO;
import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.pojo.UserInfo;
import com.nexon.nutriai.pojo.dto.UserProfileDTO;
import com.nexon.nutriai.dao.entity.AppUser;
import com.nexon.nutriai.dao.entity.UserHealthGoal;
import com.nexon.nutriai.dao.entity.UserProfile;
import com.nexon.nutriai.service.UserService;
import com.nexon.nutriai.service.oauth2.OAuth2Service;
import com.nexon.nutriai.util.JwtUtil;
import com.nexon.nutriai.util.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/web/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Map<String, OAuth2Service> oAuth2Services;
    private final Cache<String, String> openIdCache;

    /**
     * 用户注册接口
     */
    @PostMapping("/signUp")
    public BaseResponse<Object> register(
            @RequestPart String phone,
            @RequestPart String name,
            @RequestPart String password) {

        userService.signUp(phone, name, password);

        return new BaseResponse<>(ErrorCode.SUCCESS, "注册成功");
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/signIn")
    public BaseResponse<UserInfo> signIn(@RequestPart String phone, @RequestPart String password, ServerHttpResponse response) {

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
    public BaseResponse<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, ServerHttpResponse response) {

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
    public BaseResponse<?> logout(@RequestPart String phone) {
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
        userService.updateUserProfile(userProfileDTO);
        return new BaseResponse<>(ErrorCode.SUCCESS, "更新成功");
    }

    /**
     * 更新目标
     * @param userHealthGoalDTO
     * @return
     */
    @PostMapping("/updateUserHealthGoal")
    public BaseResponse<?> updateUserHealthGoal(@RequestBody UserHealthGoalDTO userHealthGoalDTO) {
        userService.updateUserHealthGoal(userHealthGoalDTO);
        return new BaseResponse<>(ErrorCode.SUCCESS, "更新成功");
    }

    /**
     * 获取用户信息
     * @param phone
     * @return
     */
    @GetMapping("/getUserProfile")
    public BaseResponse<UserProfile> getUserProfile(@RequestParam String phone) {
        UserProfile userProfile = userService.getUserProfile(phone);
        return new BaseResponse<>(userProfile);
    }

    /**
     * 获取用户目标
     * @param phone
     * @return
     */
    @GetMapping("/getUserHealthGoal")
    public BaseResponse<UserHealthGoal> getUserHealthGoal(@RequestParam String phone) {
        UserHealthGoal userHealthGoal = userService.getUserHealthGoal(phone);
        return new BaseResponse<>(userHealthGoal);
    }

    // OAuth 2.0 鉴权

    @GetMapping("/callback/{provider}")
    public Mono<Void> oauthCallback(
            @PathVariable String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            ServerWebExchange exchange) {

        ServerHttpResponse response = exchange.getResponse();

        // 1. 根据提供商获取对应的服务
        OAuth2Service oAuth2Service = oAuth2Services.get(provider + "OAuth2Service");
        if (oAuth2Service == null) {
            // 处理不支持的提供商
            return handleError(response, HttpStatus.BAD_REQUEST, "Unsupported OAuth2 provider: " + provider);
        }

        AtomicReference<String> providerUserIdRef = new AtomicReference<>();

        // 2. 执行通用的 OAuth2 流程
        return oAuth2Service.getAccessToken(code)
                .flatMap(responseNode -> {
                    String openId = responseNode.get("openid").asText();
                    providerUserIdRef.set(openId);
                    // 【关键改动】调用我们包装好的安全方法
                    return Mono.fromCallable(() -> {
                                // 在这里执行您的阻塞调用
                                // 这个 Callable 里的代码会在一个独立的线程池中运行
                                return userService.findByOpenId(openId);
                            })
                            .subscribeOn(Schedulers.boundedElastic()); // 关键：指定在弹性线程池中执行
                })
                .defaultIfEmpty(null)
                .flatMap(user -> {
                    if (user != null) {
                        // --- 用户存在，执行登录逻辑 ---
                        String phone = user.getPhone();
                        Map<String, String> tokens = jwtUtil.generateToken(phone);
                        setToken(response, tokens);
                        String redirectUrl = "https://yourdomain.com/login-success";
                        return ServerResponse.temporaryRedirect(URI.create(redirectUrl)).bodyValue(exchange).then();
                    } else {
                        // --- 用户不存在，执行重定向逻辑 ---
                        // 现在 handleUserNotFound 返回 Mono<Void> 是完全正确的
                        return handleUserNotFound(response, providerUserIdRef.get(), state);
                    }
                });
    }

    /**
     * 处理新用户绑定手机号的逻辑
     */
    private Mono<Void> handleUserNotFound(ServerHttpResponse response, String openId, String state) {
        // 将 openId 和 state 临时存储到 Redis 或 Session 中，有效期5分钟
        openIdCache.put("wechat_bind:" + state, openId, 5 * 60 * 1000);

        // 重定向到前端绑定手机号页面，并把 state 带过去
        String redirectUrl = String.format("https://yourdomain.com/bind-phone?state=%s", state);
        return ServerResponse.temporaryRedirect(URI.create(redirectUrl)).bodyValue(response).then();
    }

    /**
     * 通用的错误处理方法
     * @param response ServerHttpResponse 对象
     * @param status HTTP 状态码 (如 BAD_REQUEST, UNAUTHORIZED)
     * @param message 错误信息
     * @return Mono<Void>
     */
    private Mono<Void> handleError(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        // 构建统一的错误响应体
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", status.value(), message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 新增接口：用户提交手机号和验证码，完成绑定
     */
    @PostMapping("/bindPhone")
    public BaseResponse<?> bindPhone(
            @RequestPart String phone,
            @RequestPart String verificationCode,
            @RequestPart String state, String openId) {

        // 1. 校验验证码
        // if (!smsService.checkCode(phone, verificationCode)) { ... }

        // 2. 从 Redis 中获取 openId
        if (null == openId) {
            openId = openIdCache.get("wechat_bind:" + state);
        }

        // if (openId == null) { ... }

        // 3. 将 openId 更新到对应用户上
        boolean flag = userService.updateOpenId(phone, openId);
        if (!flag) {
            return new BaseResponse<>(ErrorCode.USER_NOT_FIND_ERROR);
        }

        // 4. 删除 Redis 中的临时数据
        openIdCache.remove("wechat_bind:" + state);

        // 5. 绑定成功，自动登录
        userService.signInWithOpenId(phone, openId);
         Map<String, String> tokens = jwtUtil.generateToken(phone);
         return new BaseResponse<>(tokens);
    }

    private void setToken(ServerHttpResponse response, Map<String, String> tokens) {
        response.getHeaders().add("Authorization", "Bearer " + tokens.get("accessToken"));

        // 创建 HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true) // 关键！JS 无法读取
                .secure(true)   // 关键！只能在 HTTPS 下传输
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7天
                .sameSite("Lax")
                .build();

        response.getHeaders().add("Set-Cookie", cookie.toString());
    }
}
