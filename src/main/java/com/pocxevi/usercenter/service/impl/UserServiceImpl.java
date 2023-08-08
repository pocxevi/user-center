package com.pocxevi.usercenter.service.impl;
import java.util.Date;
import com.aliyuncs.utils.Base64Helper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pocxevi.usercenter.common.ErrorCode;
import com.pocxevi.usercenter.exception.BusinessException;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.service.UserService;
import com.pocxevi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.pocxevi.usercenter.constant.UserConstant.ADMIN_USERROLE;
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

    @Resource
    private UserMapper userMapper;

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
        safetyUser.setTags(user.getTags());
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
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> queryUserByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        userMapper.selectCount(null); // 进行一次空查询，避免数据库连接时间的影响
        // 1.先查询所有用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Gson gson = new Gson();
        List<User> result;
        long startTime = System.currentTimeMillis();
        List<User> userList = userMapper.selectList(wrapper);

        // 2.在内存中判断是否包含要求的标签
        result = userList.stream().filter(user -> {
            if (StringUtils.isBlank(user.getTags())) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {}.getType());
            tempTagNameSet.retainAll(tagNameList); // 求两个集合的交集
            return !tempTagNameSet.isEmpty();
        }).map(this::getSafetyUser).collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        log.info("内存查询结果" + result);
        log.info("内存查询时间" + (endTime - startTime));

        // 数据库 模糊查询
        startTime = System.currentTimeMillis();
        for (String tagName : tagNameList) {
            wrapper.like("tags", tagName);
        }
        userList = userMapper.selectList(wrapper);
        result = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        endTime = System.currentTimeMillis();
        log.info("内存查询结果" + result);
        log.info("内存查询时间" + (endTime - startTime));
        return result;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> queryUsersByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1.先查询所有用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Gson gson = new Gson();
        List<User> result;
        long startTime = System.currentTimeMillis();
        List<User> userList = userMapper.selectList(wrapper);

        // 2.在内存中判断是否包含要求的标签
        result = userList.stream().filter(user -> {
            if (StringUtils.isBlank(user.getTags())) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {}.getType());
            tempTagNameSet.retainAll(tagNameList); // 求两个集合的交集
            return !tempTagNameSet.isEmpty();
        }).map(this::getSafetyUser).collect(Collectors.toList());

        return result;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();

        if (userId <=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前用户（自己的信息）
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object loginUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User)loginUser;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);// 不能用userServiceImpl里的常量
        User user = (User) userObj;
//        if (user == null || user.getUserRole() != 1)  { // 为了避免出现空指针异常，先进行判空
//            return false;
//        }
//        return true;
        return user != null || user.getUserRole().equals(ADMIN_USERROLE);
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null || loginUser.getUserRole().equals(ADMIN_USERROLE);
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




