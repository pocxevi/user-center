package com.pocxevi.usercenter.service;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.aliyuncs.utils.Base64Helper;
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
        user.setUserPassword("xxx");
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
    
    @Test
    void testMD5() {
        String encodeStr = "";
        String str = "aaaaa";
        byte[] utfBytes = str.getBytes();
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
        System.out.println(encodeStr);
    }

    @Test
    void userRegister() {
        String userAccount = "jkjkk";
        String userPassword = "123456789";
        String checkPassword = "123456789";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        Assertions.assertTrue(result > 0);

    }

    @Test
    void queryUserByTags() {
        List<String> list = Arrays.asList("java", "python");
        List<User> users = userService.queryUserByTags(list);
        System.out.println(users);
    }
}