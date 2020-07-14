package com.zy.integrate.controller;

import com.zy.integrate.domain.TestPO;
import com.zy.integrate.service.RedisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author zhangyong
 * @Date 2020/7/14 22:49
 */
@RestController
@CrossOrigin
public class RedisController {
    @Resource
    private RedisService redisService;

    /**
     * /test/redis
     */
    @GetMapping("/redis")
    public Map<String,String> test(){
        return this.redisService.test();
    }
    @GetMapping("/redis/cache")
    public String testCache(){
        return this.redisService.cacheRedis();
    }

    @GetMapping("/redis/set")
    public Set<String> testSet(){
        return this.redisService.testSet();
    }

    @GetMapping("/redis/list")
    public List<String> testList(){
        return this.redisService.testList();
    }

    @GetMapping("/redis/hash")
    public String testHash(){
        return this.redisService.testHash();
    }

    @GetMapping("/redis/zset")
    public Set<String> testZset(){
        return this.redisService.testZset();
    }

}
