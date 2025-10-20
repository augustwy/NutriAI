package com.nexon.nutriai.exception;

import com.nexon.nutriai.constant.ErrorCode;

public class NutriaiException extends RuntimeException {

    private String message;

    private String code;

    public NutriaiException(String code, String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public NutriaiException(String message) {
        super(message);
        this.message = message;
        this.code = ErrorCode.UNKNOWN_ERROR;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
