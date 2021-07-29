package com.zy.integrate.controller;

import com.zy.integrate.domain.TestPO;
import com.zy.integrate.service.RedisService;
import com.zy.integrate.service.TestService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    private TestService testService;



    @GetMapping("/test")
    public List<TestPO> testGet(@RequestBody @Valid TestPO po){
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

    @GetMapping("/test/test")
    public String test(){
        throw new IllegalArgumentException("sss");
//        return "success";
    }
}
