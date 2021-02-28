package com.zy.integrate.service;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;

/**
 * @author zhangyong05
 * Created on 2021-02-24
 */
@Service
public class TopicListener implements ApplicationRunner,Order {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public void run(ApplicationArguments args){
        RTopic topic = redissonClient.getTopic("test-topic");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                System.out.println("CharSequence序列： " + charSequence);
                System.out.println("收到消息：" + msg);
            }
        });
    }

    @Override
    public int value() {
        return 1;
    }
}
