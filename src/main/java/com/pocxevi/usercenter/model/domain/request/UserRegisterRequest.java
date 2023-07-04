package com.pocxevi.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
/**
 * 用户注册请求体
 *
 * @Auther pocxevi
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4989318179519434124L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 校验密码
     */
    private String checkPassword;
}
