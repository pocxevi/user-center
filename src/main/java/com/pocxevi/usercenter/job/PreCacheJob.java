package com.pocxevi.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pocxevi.usercenter.config.RedissonConfig;
import com.pocxevi.usercenter.model.domain.User;
import com.pocxevi.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private UserService userService;

    private List<Long> primaryUser = Arrays.asList(1L);

    @Scheduled(cron = "0 4 0 * * ? *")
    public void doCacheRecommendUser() {
        // 添加分布式锁
        RLock lock = redissonClient.getLock("partnerMatching:precachejob:lock");

        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {

                // 执行缓存预热
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                for (Long userId : primaryUser) {
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(3,4), userQueryWrapper);

                    String redisKey = String.format("partnerMatching:user:recommend%s", userId);
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 3000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set error:", e); // 编译时期加上这个对象
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error");
        } finally {
            // 只能释放自己的锁 必须放在finally，否则任务执行失败就执行不到了
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

}
