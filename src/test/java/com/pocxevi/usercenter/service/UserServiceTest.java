package com.pocxevi.usercenter.service;
import java.util.Date;

import com.pocxevi.usercenter.model.domain.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class UserServiceTest {
    @Resource
    public UserService userService;
    @Test
    public void addTest() {
        User user = new User();
        user.setId(0L);
        user.setUserAccount("pocxevi");
        user.setUsername("pocxevi");
        user.setAvatarUrl("https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1cts0X.img?w=1920&h=1080&q=60&m=2&f=jpg");
        user.setGender(0);
        user.setPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);

        boolean save = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(save);

    }
}