package com.zy.integrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IntegrateApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrateApplication.class, args);
    }

}
