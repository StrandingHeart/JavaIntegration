package com.zy.integrate.controller;

import com.zy.integrate.domain.TestPO;
import com.zy.integrate.service.TestService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this is demo test Controller
 */
@CrossOrigin
@RestController
public class TestController {

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    @Resource
    public TestService testService;

    /**
     * /test/redis
     */
    @GetMapping("/redis")
    public Map<String,String> test(){
        stringRedisTemplate.opsForValue().set("a","zy");
        Map<String,String> map = new HashMap<>();
        map.put("zy",stringRedisTemplate.opsForValue().get("a"));
        return map;
    }

    @GetMapping("/test")
    public List<TestPO> testGet(){
        this.testService.insertTest();
        return this.testService.findAll();
    }

    @GetMapping("/test/name")
    public TestPO testGetByName(@RequestParam(name = "name") String name){
        return this.testService.getByName(name);
    }

    @GetMapping("/test/age")
    public List<TestPO> testGetByName(@RequestParam(name = "age") Byte age){
        return this.testService.getByAge(age);
    }
}
