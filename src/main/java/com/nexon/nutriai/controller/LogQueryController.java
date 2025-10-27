package com.nexon.nutriai.controller;

import com.nexon.nutriai.dao.entity.DialogueDetail;
import com.nexon.nutriai.dao.entity.DialogueSession;
import com.nexon.nutriai.service.LogQueryService;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 日志查询控制器
 * 
 * 提供对话记录和详情的查询接口，支持按用户、对话类型等条件进行查询。
 */
@Slf4j
@RestController
@RequestMapping("/web/log")
@RequiredArgsConstructor
public class LogQueryController {

    private final LogQueryService logQueryService;

    /**
     * 分页查询当前用户的对话记录
     * 
     * @param exchange ServerWebExchange对象，用于获取当前用户信息
     * @param methodName 对话类型（方法名，可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @return 对话记录分页结果
     */
    @GetMapping("/sessions")
    public Mono<ResponseEntity<Page<DialogueSession>>> queryCurrentUserDialogueSessions(
            ServerWebExchange exchange,
            @RequestParam(required = false) String methodName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String phone = WebFluxUtil.getPhone(exchange);
        if (phone == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        Page<DialogueSession> sessions = logQueryService.queryDialogueSessionsByUser(phone, methodName, page, size);
        return Mono.just(ResponseEntity.ok(sessions));
    }

    /**
     * 分页查询指定用户的对话记录
     * 
     * @param phone 用户手机号
     * @param methodName 对话类型（方法名，可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @return 对话记录分页结果
     */
    @GetMapping("/sessions/{phone}")
    public Mono<ResponseEntity<Page<DialogueSession>>> queryDialogueSessionsByUser(
            @PathVariable String phone,
            @RequestParam(required = false) String methodName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DialogueSession> sessions = logQueryService.queryDialogueSessionsByUser(phone, methodName, page, size);
        return Mono.just(ResponseEntity.ok(sessions));
    }

    /**
     * 查询指定会话的详细对话内容（分页）
     * 
     * @param sessionId 会话ID
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @return 对话详情分页结果
     */
    @GetMapping("/details/{sessionId}")
    public Mono<ResponseEntity<Page<DialogueDetail>>> queryDialogueDetailsBySession(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DialogueDetail> details = logQueryService.queryDialogueDetailsBySession(sessionId, page, size);
        return Mono.just(ResponseEntity.ok(details));
    }

    /**
     * 查询指定会话的所有对话详情（适用于数据量较小的场景）
     * 
     * @param sessionId 会话ID
     * @return 对话详情列表
     */
    @GetMapping("/details/{sessionId}/all")
    public Mono<ResponseEntity<List<DialogueDetail>>> queryAllDialogueDetailsBySession(@PathVariable String sessionId) {
        List<DialogueDetail> details = logQueryService.queryAllDialogueDetailsBySession(sessionId);
        return Mono.just(ResponseEntity.ok(details));
    }

    /**
     * 根据会话ID查询对话记录
     * 
     * @param sessionId 会话ID
     * @return 对话记录
     */
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<DialogueSession>> queryDialogueSessionById(@PathVariable String sessionId) {
        return Mono.just(logQueryService.queryDialogueSessionById(sessionId))
                .map(session -> session.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()));
    }
}