package com.nexon.nutriai.constant;

public class ErrorCode {

    /**
     * 成功
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * 未知异常
     */
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    /*** 图片识别异常 ***/

    /**
     * 图片为空
     */
    public static final String IMAGE_EMPTY = "IMAGE_EMPTY";

    /**
     * 图片识别错误
     */
    public static final String IMAGE_RECOGNITION_ERROR = "IMAGE_RECOGNITION_ERROR";


    /*** 登录异常 ***/

    /**
     * 注册失败
     */
    public static final String SIGN_UP_ERROR = "SIGN_UP_ERROR";

    /**
     * 登录失败
     */
    public static final String SIGN_IN_ERROR = "SIGN_IN_ERROR";

    public static final String TOKEN_REFRESH_ERROR = "TOKEN_REFRESH_ERROR";
}
