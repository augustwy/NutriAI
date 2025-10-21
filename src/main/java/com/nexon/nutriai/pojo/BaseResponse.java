package com.nexon.nutriai.pojo;

import com.nexon.nutriai.constant.ErrorCode;

public class BaseResponse<T> {

    private String code;

    private String message;

    private T data;

    public BaseResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse(String code) {
        this.code = code;
        // todo i8n 文件转换
        this.message = code;
    }

    public BaseResponse(T data) {
        this.code = code;
        // todo i8n 文件转换
        this.message = code;

        this.data = data;
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
