package com.pocxevi.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pocxevi.usercenter.common.BaseResponse;
import com.pocxevi.usercenter.common.ErrorCode;
import com.pocxevi.usercenter.common.ResultUtils;
import com.pocxevi.usercenter.exception.BusinessException;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.model.domain.request.UserLoginRequest;
import com.pocxevi.usercenter.model.domain.request.UserRegisterRequest;
import com.pocxevi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.pocxevi.usercenter.constant.UserConstant.ADMIN_USERROLE;
import static com.pocxevi.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author yy187
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // message自定义，在这里可以看得更清楚一点
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);

    }

    /**
     * 用户登录
     *
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User currentUser = (User) userObj;
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);

        return ResultUtils.success(safetyUser);
    }

    /**
     * 用户查询
     *
     * @param username
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        // 权限判断
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) { // 既判断长度，也判断null
            userQueryWrapper.like("username", username); // 模糊查询，left和right要用另一个方法
        }
        List<User> users = userService.list(userQueryWrapper);

        List<User> userList = users.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(long id, HttpServletRequest request) {
        // 权限判断
        if (isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 权限判断 是否为管理员
     *
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
        return user != null || user.getUserRole().equals(ADMIN_USERROLE);

    }
}
