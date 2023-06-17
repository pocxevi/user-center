package com.pocxevi.usercenter.service;

import com.pocxevi.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yy187
* @description 针对表【user】的数据库操作Service
* @createDate 2023-06-13 17:08:10
*/
public interface UserService extends IService<User> {

    /**
     * 用户注释
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
     long userRegister(String userAccount, String userPassword, String checkPassword);
}
