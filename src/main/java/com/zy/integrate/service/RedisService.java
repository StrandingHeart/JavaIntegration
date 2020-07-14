package com.zy.integrate.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.zy.integrate.domain.TestPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    /**
     * spring操作redis string
     * @return
     */
    public Map<String,String> test(){
        stringRedisTemplate.opsForValue().set("a","zy");
        Map<String,String> map = new HashMap<>();
        map.put("zy",stringRedisTemplate.opsForValue().get("a"));
        map.put("cacheRedis",stringRedisTemplate.opsForValue().get("cacheRedis"));
        return map;
    }

    /**
     * spring操作redis list
     * @return
     */
    public List<String> testList(){
        stringRedisTemplate.opsForList().leftPush("test_list","1");
        stringRedisTemplate.opsForList().leftPush("test_list","2");
        stringRedisTemplate.opsForList().leftPush("test_list","3");
        return stringRedisTemplate.opsForList().range("test_list",0,-1);
    }

    /**
     * hash
     * @return
     */
    public String testHash(){
        stringRedisTemplate.opsForHash().put("test_hash","a","123456");
        return (String) stringRedisTemplate.opsForHash().get("test_hash", "a");
    }

    /**
     * set
     * @return
     */
    public Set<String> testSet(){
        stringRedisTemplate.opsForSet().add("test_set","a");
        stringRedisTemplate.opsForSet().add("test_set","b");
        stringRedisTemplate.opsForSet().add("test_set2","b");
        stringRedisTemplate.opsForSet().add("test_set2","c");
        //求个差集
        return stringRedisTemplate.opsForSet().intersect("test_set", "test_set2");
    }

    /**
     * zset
     * @return
     */
    public Set<String> testZset(){
        stringRedisTemplate.opsForZSet().add("test_zset","swt",90);
        stringRedisTemplate.opsForZSet().add("test_zset","zy",100);
        stringRedisTemplate.opsForZSet().add("test_zset","lzy",70);
        stringRedisTemplate.opsForZSet().add("test_zset","lbw",80);
        stringRedisTemplate.opsForZSet().add("test_zset","lbh",60);
        return stringRedisTemplate.opsForZSet().rangeByScore("test_zset", 70, 100);
    }


    /**
     * jetcache 缓存方法结果
     */
    @Cached(name = "cacheRedis", expire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    public String cacheRedis(){
        //如果生效了，下次就不会执行而是直接从Redis里面取结果了。通过第二次调用是否打印日志来判断。
        logger.info("enter cacheRedis");
        return "zhangyong";
    }
}
