package com.nexon.nutriai.exception;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(NutriaiException.class)
    public ResponseEntity<BaseResponse> handleBusinessException(NutriaiException e) {
        BaseResponse error = new BaseResponse(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.OK);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGenericException(Exception e) {
        log.error("handleGenericException", e);
        BaseResponse error = new BaseResponse(ErrorCode.UNKNOWN_ERROR, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.OK);
    }
}
