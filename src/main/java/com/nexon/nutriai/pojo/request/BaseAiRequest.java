package com.nexon.nutriai.pojo.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BaseAiRequest extends BaseRequest {

    /**
     * 对话内容
     */
    private String content;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 额外的上下文信息
     */
    private Map<String, Object> context;

    public BaseAiRequest() {

    }

    public BaseAiRequest(BaseRequest baseRequest) {
        this.setPhone(baseRequest.getPhone());
        this.setChatId(baseRequest.getChatId());
        this.setOpenId(baseRequest.getOpenId());
    }
}
