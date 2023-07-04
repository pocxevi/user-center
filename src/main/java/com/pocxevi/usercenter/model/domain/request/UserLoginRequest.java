package com.pocxevi.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @Auther pocxevi
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 4594885497033590418L;

    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
}
