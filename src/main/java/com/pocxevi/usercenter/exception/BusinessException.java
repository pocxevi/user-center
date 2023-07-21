package com.pocxevi.usercenter.exception;

import com.pocxevi.usercenter.common.ErrorCode;
import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private final int code;

    private final String description;

    /**
     * 自定义message、code、description
     * @param message
     * @param code
     * @param description
     */
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    /**
     * message、code、description都是根据传入的ErrorCode来的
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    /**
     * 自定义description，message、code都是根据传入的ErrorCode来的
     * @param errorCode
     */
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
