package com.nexon.nutriai.pojo;

public class BaseResponse<T> {

    private String code;

    private String message;

    private T data;

    public BaseResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(T data) {
        this.code = "00000";
        this.message = "成功";
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
