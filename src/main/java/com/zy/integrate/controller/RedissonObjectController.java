package com.zy.integrate.controller;

import com.zy.integrate.domain.Persion;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangyong05
 * Created on 2021-02-24 
 * https://www.javadoc.io/doc/org.redisson/redisson/3.10.3/org/redisson/api/RTopic.html
 * 可以参考： https://github.com/redisson/redisson-examples
 */
@RestController
public class RedissonObjectController {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取keys && 根据pattern 获取keys
     * @param 
     * @return  
     * @author zhangyong05 
     * 2021/2/26 
     */
    @GetMapping("/redisson-key")
    public void keyTest(){
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keysByPattern = keys.getKeysByPattern("s*");
        for (String s : keysByPattern) {
            System.out.println(s);
        }
    }

    /**
     * 验证redis 的string类型操作
     * @author zhangyong05 
     * 2021/2/26 
     */
    @GetMapping("/redisson-string")
    public String stringTest(){
        // https://stackoverflow.com/questions/51276593/whats-the-usage-for-tryset-method-in-redisson-rbucket
        RBucket<String> bucket = redissonClient.getBucket("string-test");
        // trySet当value为空时会设置成功
        boolean trySetValue = bucket.trySet("trySetValue");
        System.out.println(trySetValue);
        String res = bucket.get();
        bucket.compareAndSet("except","update");
        String before = bucket.getAndSet("after");
        // size返回的是对象的大小所占字节，并非是长度。
        System.out.println(bucket.size());
        System.out.println(before);
        bucket.set(System.currentTimeMillis() + "asd");
        return res;
    }

    /**
     * 验证 Redis的 string存储自定义对象的操作,需要注意的是redisson的codec间接决定了能否存储对象，以及编解码方式
     * codec编解码配置需要一致才能正常序列化和反序列化
     * @author zhangyong05 
     * 2021/2/26 
     */
    @GetMapping("/redisson-object")
    public Persion objectTest(){
        // redisson的默认编码codec支持对象存Redis的string类型里面
        Persion persion = new Persion();
        persion.setName("张三");
        persion.setTime(System.currentTimeMillis());
        persion.setAge(18);
        RBucket<Persion> bucket = redissonClient.getBucket("object-test");
        Persion res = bucket.get();
        bucket.set(persion);
        return res;
    }

    /**
     * 验证原子类，也是Redis中的string类型存储
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-atomic-long")
    public Long atomicLongTest(){
        RAtomicLong atomicLong = redissonClient.getAtomicLong("atomic-long-test");
        atomicLong.addAndGet(1);
        atomicLong.addAndGet(1);
        return atomicLong.get();
    }

    /**
     * 验证发布订阅，调用这个接口就能触发，因为写了一个TopicService类在程序启动时去运行监听topic订阅方
     * 这个接口是用来publish消息的
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-topic")
    public void topicTest() {
        RTopic topic = redissonClient.getTopic("test-topic");
        // 程序启动时有ApplicationRunner实现类注册监听器：topicListener
        topic.publish("msg");
        // 也可以用topic模式，同一模式下都会收到消息
    }

}
