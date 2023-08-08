package com.pocxevi.usercenter.service;

import com.pocxevi.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.pocxevi.usercenter.constant.UserConstant.ADMIN_USERROLE;
import static com.pocxevi.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author yy187
* @description 针对表【user】的数据库操作Service
* @createDate 2023-06-13 17:08:10
*/
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
     long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
     User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户信息脱敏
     *
     * @param user
     * @return
     */
     User getSafetyUser(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
     int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    List<User> queryUserByTags(List<String> tagNameList);

    /**
     * 根据标签搜索用户（内存过滤 ）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    List<User> queryUsersByTags(List<String> tagNameList);

    /**
     *
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 权限判断 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 权限判断 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);
}
