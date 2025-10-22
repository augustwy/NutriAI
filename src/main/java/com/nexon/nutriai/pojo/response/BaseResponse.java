package com.nexon.nutriai.pojo.response;

import com.nexon.nutriai.constant.ErrorCode;
import reactor.core.publisher.Mono;

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
        this.code = ErrorCode.SUCCESS;
        // todo i8n 文件转换
        this.message = ErrorCode.SUCCESS;

        this.data = data;
    }

    public static BaseResponse<Object> success() {
        return new BaseResponse<>(ErrorCode.SUCCESS);
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

    public void setData(T data) {
        this.data = data;
    }


    public Mono<BaseResponse<T>> toMono() {
        return Mono.just(this);
    }
}
