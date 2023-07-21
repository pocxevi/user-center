package com.pocxevi.usercenter.common;

/**
 * 返回 封装响应 工具类
 * ExceptionHandler中返回响应可以用，更方便返回
 * @author pocxevi
 */
public class ResultUtils<T> {

    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(),null, message, description);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }


}
