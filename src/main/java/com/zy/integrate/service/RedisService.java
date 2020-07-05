package com.zy.integrate.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    public Map<String,String> test(){
        stringRedisTemplate.opsForValue().set("a","zy");
        Map<String,String> map = new HashMap<>();
        map.put("zy",stringRedisTemplate.opsForValue().get("a"));
        map.put("cacheRedis",stringRedisTemplate.opsForValue().get("cacheRedis"));
        return map;
    }
    @Cached(name = "cacheRedis", expire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    public String cacheRedis(){
        //如果生效了，下次就不会执行而是直接从Redis里面取结果了。通过第二次调用是否打印日志来判断。
        logger.info("enter cacheRedis");
        return "zhangyong";
    }
}
