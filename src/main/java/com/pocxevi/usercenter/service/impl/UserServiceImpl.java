package com.pocxevi.usercenter.service.impl;
import java.util.Date;
import com.aliyuncs.utils.Base64Helper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pocxevi.usercenter.common.ErrorCode;
import com.pocxevi.usercenter.exception.BusinessException;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.service.UserService;
import com.pocxevi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pocxevi.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author yy187
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-06-13 17:08:10
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1.先进行校验
        // 先进行判空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户密码不能为空！");
        }

        // 账户长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不小于4位！");
        }

        // 密码长度不小于八位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于八位！");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        boolean result = matcher.find();
        if (result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符！");
        }

        // 两次密码输入相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不相同！");
        }

        // 账户不能重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已重复！");
        }

        // 2.密码进行加密
        String encryptPassword = MD5Base64(userPassword);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean savedResult = this.save(user);

        // 为了防止装箱错误(返回null值)
        if (!savedResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据错误！");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.先进行校验
        // 先进行判空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户密码不能为空！");
        }

        // 账户长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度不能小于4位！");
        }

        // 密码长度不小于八位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于八位！");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        boolean result = matcher.find();
        if (result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不能包含特殊字符！");
        }

        // 2.登录：查询、返回
        // 密码加密后验证
        String encryptPassword = MD5Base64(userPassword);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        wrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(wrapper);

        // 用户为空 (记录日志尽量用英文，不会出现乱码)
        if (user == null) {
            log.info("user login faild, userAccount can not match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在！");
        }
        // 3.用户信息脱敏
        User safetyUser = getSafetyUser(user);
        // 4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户信息脱敏
     *
     * @param user
     * @return
     */
    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在！");
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(new Date());
        safetyUser.setUpdateTime(new Date());
        safetyUser.setUserRole(user.getUserRole());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 用户密码加密
     * @param s
     * @return
     */
    public static String MD5Base64(String s) {
        if (s == null) {
            return null;
        }
        String encodeStr = "";
        byte[] utfBytes = s.getBytes();
        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(utfBytes);
            byte[] md5Bytes = mdTemp.digest();
            encodeStr = Base64Helper.encode(md5Bytes);
        } catch (Exception e) {
            throw new Error("Failed to generate MD5 : " + e.getMessage());
        }
        return encodeStr;
    }



}




