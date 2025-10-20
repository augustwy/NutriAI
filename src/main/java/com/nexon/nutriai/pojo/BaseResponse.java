package com.nexon.nutriai.pojo;

import com.nexon.nutriai.constant.ErrorCode;

public class BaseResponse {

    private String code;

    private String message;

    public BaseResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse(String code) {
        this.code = code;
        // todo i8n 文件转换
        this.message = code;
    }

    public static BaseResponse success() {
        return new BaseResponse(ErrorCode.SUCCESS);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
