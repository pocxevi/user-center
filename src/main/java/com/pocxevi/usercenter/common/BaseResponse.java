package com.pocxevi.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * 所有的其他类：ResultUtils、BusinessException、ExceptionHandler都是为了在构造函数中传入参数（一个一个值传或枚举类传)，
 * 生成BaseResponse返回给前端
 * @param <T>
 * @author pocxevi
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    // 构造函数，不管传几个参数都要在实现里将所有属性初始化
    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }


}
