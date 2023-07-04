package com.pocxevi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pocxevi.usercenter.constant.UserConstant;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.model.domain.request.UserLoginRequest;
import com.pocxevi.usercenter.model.domain.request.UserRegisterRequest;
import com.pocxevi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pocxevi.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author yy187
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @return
     */
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null) {
            return null;
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return result;

    }

    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public User userRegister(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null) {
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return user;

    }

    /**
     * 用户查询
     *
     * @param username
     * @return
     */
    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request){
        // 权限判断
        if (isAdmin(request))  {
            return new ArrayList<>();
           }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) { // 既判断长度，也判断null
            userQueryWrapper.like("username", username); // 模糊查询，left和right要用另一个方法
        }
        List<User> users = userService.list(userQueryWrapper);

        return users.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
    }

    @PostMapping("/delete")
    public boolean deleteUser(long id, HttpServletRequest request){
        // 权限判断
        if (isAdmin(request))  {
            return false;
        }

        if (id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    /**
     * 权限判断
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);// 不能用userServiceImpl里的常量
        User user = (User) userObj;
//        if (user == null || user.getUserRole() != 1)  { // 为了避免出现空指针异常，先进行判空
//            return false;
//        }
//        return true;
        return user == null || user.getUserRole() != 1;
    }
}
