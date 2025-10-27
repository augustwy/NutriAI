package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.DialogueDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DialogueDetailRepository extends JpaRepository<DialogueDetail, Long> {
    /**
     * 根据会话ID查询所有对话详情，按序列号排序
     * 
     * @param sessionId 会话ID
     * @return 对话详情列表
     */
    List<DialogueDetail> findBySessionIdOrderBySequence(String sessionId);
    
    /**
     * 根据会话ID查询对话详情（分页），按序列号排序
     * 
     * 适用于大数据量场景，避免一次性加载过多数据
     * 
     * @param sessionId 会话ID
     * @param pageable 分页参数
     * @return 对话详情分页结果
     */
    Page<DialogueDetail> findBySessionId(String sessionId, Pageable pageable);
}