package com.pocxevi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.service.UserService;
import com.pocxevi.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author yy187
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-06-13 17:08:10
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




