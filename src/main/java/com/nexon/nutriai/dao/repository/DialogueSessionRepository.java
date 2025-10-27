package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.DialogueSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DialogueSessionRepository extends JpaRepository<DialogueSession, Long> {
    Optional<DialogueSession> findBySessionId(String sessionId);
    
    /**
     * 根据用户手机号查询对话记录（分页）
     * 
     * @param phone 用户手机号
     * @param pageable 分页参数
     * @return 对话记录分页结果
     */
    Page<DialogueSession> findByPhone(String phone, Pageable pageable);
    
    /**
     * 根据用户手机号和方法名查询对话记录（分页）
     * 
     * @param phone 用户手机号
     * @param methodName 方法名
     * @param pageable 分页参数
     * @return 对话记录分页结果
     */
    Page<DialogueSession> findByPhoneAndMethodName(String phone, String methodName, Pageable pageable);
}