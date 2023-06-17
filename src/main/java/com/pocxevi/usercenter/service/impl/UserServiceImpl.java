package com.pocxevi.usercenter.service.impl;
import com.aliyuncs.utils.Base64Helper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.service.UserService;
import com.pocxevi.usercenter.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author yy187
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-06-13 17:08:10
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1.先进行校验
        // 先进行判空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return -1;
        }

        // 账户长度不小于4位
        if (userAccount.length() < 4) {
            return -1;
        }

        // 密码长度不小于八位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }

        // 密码中不包含的特殊字符正则表达式
        String specialCharsRegex = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

        // 检查密码是否包含特殊字符
        Pattern pattern = Pattern.compile(specialCharsRegex);
        Matcher matcher = pattern.matcher(userAccount);
        boolean result = matcher.find();

        if (result) {
            return -1;
        }

        // 两次密码输入相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }

        // 账户不能重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        long count = this.count(wrapper);
        if (count > 0) {
            return -1;
        }

        // 2.密码进行加密
        String encryptPassword = MD5Base64(userPassword);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);

        boolean savedResult = this.save(user);

        // 为了防止装箱错误
        if (!savedResult) {
            return -1;
        }

        return user.getId();
    }

    public static String MD5Base64(String s) {
        if (s == null) return null;
        String encodeStr = "";
        byte[] utfBytes = s.getBytes();
        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(utfBytes);
            byte[] md5Bytes = mdTemp.digest();
            Base64Helper b64Encoder = new Base64Helper();
            encodeStr = b64Encoder.encode(md5Bytes);
        } catch (Exception e) {
            throw new Error("Failed to generate MD5 : " + e.getMessage());
        }
        return encodeStr;
    }
}




