package com.nexon.nutriai.service;

import com.nexon.nutriai.dao.entity.DialogueDetail;
import com.nexon.nutriai.dao.entity.DialogueSession;
import com.nexon.nutriai.dao.repository.DialogueDetailRepository;
import com.nexon.nutriai.dao.repository.DialogueSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 日志查询服务类
 * 
 * 提供对话记录和详情的查询功能，支持按用户、对话类型等条件进行查询。
 * 针对大数据量场景进行了优化处理。
 */
@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final DialogueSessionRepository dialogueSessionRepository;
    private final DialogueDetailRepository dialogueDetailRepository;

    /**
     * 分页查询指定用户的对话记录
     * 
     * @param phone 用户手机号
     * @param methodName 对话类型（方法名）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 对话记录分页结果
     */
    public Page<DialogueSession> queryDialogueSessionsByUser(String phone, String methodName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        
        if (methodName != null && !methodName.isEmpty()) {
            return dialogueSessionRepository.findByPhoneAndMethodName(phone, methodName, pageable);
        } else {
            return dialogueSessionRepository.findByPhone(phone, pageable);
        }
    }

    /**
     * 查询指定会话的详细对话内容
     * 
     * @param sessionId 会话ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 对话详情分页结果
     */
    public Page<DialogueDetail> queryDialogueDetailsBySession(String sessionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sequence"));
        return dialogueDetailRepository.findBySessionId(sessionId, pageable);
    }

    /**
     * 查询指定会话的所有对话详情（适用于数据量较小的场景）
     * 
     * @param sessionId 会话ID
     * @return 对话详情列表
     */
    public List<DialogueDetail> queryAllDialogueDetailsBySession(String sessionId) {
        return dialogueDetailRepository.findBySessionIdOrderBySequence(sessionId);
    }

    /**
     * 根据会话ID查询对话记录
     * 
     * @param sessionId 会话ID
     * @return 对话记录
     */
    public Optional<DialogueSession> queryDialogueSessionById(String sessionId) {
        return dialogueSessionRepository.findBySessionId(sessionId);
    }
}