package com.pocxevi.usercenter.service;
import java.util.Date;

import com.pocxevi.usercenter.model.domain.User;
import javafx.geometry.VPos;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        int batchSize = 500;
        ArrayList<CompletableFuture> futureList = new ArrayList<>();
        // 10000个，分成20批，每批500个，添加到list中/

        for (int i = 1; i <= INSERT_NUM/batchSize; i++) {
            List<User> userList = new ArrayList<>();
            System.out.println("先分批");
            for (int j = 1; j <= batchSize; j++) {
                User user = new User();
                user.setUserAccount("fake");
                user.setUsername("fake");
                user.setAvatarUrl("https://img2.baidu.com/it/u=1790834130,1952230725&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
                user.setGender(0);
                user.setUserPassword("asdffsss");
                user.setPhone("123455");
                user.setEmail("23123@qq.com");
                user.setTags("[]");
                user.setUserStatus(0);
                user.setUserRole(0);
                userList.add(user);
            }

            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("线程" + Thread.currentThread().getName() + "正在执行");
                userService.saveBatch(userList, batchSize);
            });

            futureList.add(future);

        }
        System.out.println("开始执行");
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
