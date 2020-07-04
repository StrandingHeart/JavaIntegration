package com.zy.integrate;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zy.integrate.*")
@EnableMethodCache(basePackages = "com.zy.integrate.service") //支持方法缓存的包
@EnableCreateCacheAnnotation
@SpringBootApplication
public class IntegrateApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrateApplication.class, args);
    }

}
