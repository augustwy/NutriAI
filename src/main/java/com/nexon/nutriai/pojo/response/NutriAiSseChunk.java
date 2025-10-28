package com.nexon.nutriai.pojo.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NutriAiSseChunk {

    /**
     * chatId
     */
    String id;

    /**
     * 固定值：nutriai.chunk
     */
    String object;

    /**
     * 消息创建时间戳
     */
    long created;

    /**
     * 消息类型
     * "message": 普通消息
     * "progress": 进度消息
     * "notice": 通知消息
     */
    String type;

    /**
     * 消息内容
     */
    Object payload;

    public NutriAiSseChunk(String id, String object, long created, String type, Object payload) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.type = type;
        this.payload = payload;
    }

    /**
     * 普通消息
     * @param content
     */
    public record Message(String content) {}

    /**
     * 进度消息
     * @param percentage
     * @param status
     */
    public record Progress(String percentage, String status) {}

    /**
     * 通知消息
     * @param type
     * @param content
     */
    public record Notice(String type, String content) {}
}
