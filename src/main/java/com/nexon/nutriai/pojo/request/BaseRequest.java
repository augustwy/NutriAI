package com.nexon.nutriai.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequest {

    /**
     * 聊天ID
     */
    private String chatId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * openId
     */
    private String openId;

}
