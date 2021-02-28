package com.zy.integrate.controller;

import org.redisson.api.*;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyong05
 * Created on 2021-02-25
 */
@RestController
public class RedissonLockController {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 分布式锁，这个接口验证的是可重入锁，跟ReentrantLock类似，Redis会记录重入次数value
     * 当释放的时候如果重入先 value-- 否则删除key
     * demo设置了10s等待锁时间，60s加锁释放时间，即使重入也60s删除
     * (问题是超过60s就释放锁不管是否业务执行完毕，所以需要控制回滚事务---后面有watchDog方式)
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-lock")
    public String lockTest(){
        System.out.println("请求触发时间: "+ LocalDateTime.now());
        RLock lock = redissonClient.getLock("test-lock-key");
        try {
            // 10是waitTime等待时间  60是 leaseTime 加锁时间，如果unlock就提前释放锁
            // 如果宕机的话，先是finally会释放锁(finally一般情况下是没问题的,可能个别极端情况有问题，我还没遇到过可以验证)，如果没释放成功的话，就leaseTime后自动释放锁。
            boolean tryLock = lock.tryLock(10, 60, TimeUnit.SECONDS);
            // tryLock执行完就加完锁了 如果返回false就加锁失败，跟Lock一样
            if (!tryLock){
                return "请稍后再试";
            }
            boolean reentrantLock = lock.tryLock(10, 60, TimeUnit.SECONDS);
            if (!reentrantLock){
                return "可重入锁失败！";
            }
            // 验证可重入锁释放在业务执行完成之前，之后再unlock就抛异常了
            Thread.sleep(75000);
            // todo something 一些有竞争的业务代码
            // 释放重入锁，value--;
            lock.unlock();
            System.out.println("释放重入锁时间: "+ LocalDateTime.now());
            // 验证锁删除在业务执行完成之前
            Thread.sleep(75000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 释放锁如果当前只有一个锁（非重入状态），会把这个hash key删掉test-lock-key
            System.out.println("释放lock时间: "+ LocalDateTime.now());
            lock.unlock();
        }
        return "success";
    }

    /**
     * 分布式锁，watchDog方式，watchDog在没指定 leaseTime的时候会触发
     * LockWatchdogTimeout控制锁的存在时间，LockWatchdogTimeout/3是检测锁续期的周期。
     * 开启watchDog会保证业务逻辑执行完成之后才释放锁。不断的检测续期。
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-watch-dog")
    public String watchDogTest(){
        // watchDog的延时时间 可以由 lockWatchdogTimeout指定默认延时时间，默认30s
        // watchDog 只有在未显示指定加锁时间时(leaseTime)才会生效；watchDog的效果是不释放锁，延长锁的时间防止并发问题.
        // watchDog 在当前线程没有执行结束的情况下，会每lockWatchdogTimeout/3时间，去检测延时，会重新设置timeout时间为30s（即刷新时间）；
        System.out.println("请求触发时间: "+ LocalDateTime.now());
        Config config = redissonClient.getConfig();
        // LockWatchdogTimeout: 30000
        // 可以验证，当调用这个接口时，把这个程序关停掉，发现Redis key的删除时间和触发时间正好LockWatchdogTimeout时长相等
        // (如果程序是触发接口10s钟后关掉的，可能触发了延期，那么就是从最近延期那个时间为起点的30s会删除key)
        // 例如 11:30调用接口，11:45关闭程序，那么key在12:15删除
        System.out.println("LockWatchdogTimeout: "+config.getLockWatchdogTimeout());
        RLock lock = redissonClient.getLock("watch-dog-test");
        try {
            // 这个时间跟reentrantLock的一致，是等待锁的时间，并非加锁时间leaseTime
            // watchDog开启时会消耗性能，可以设置leaseTime给业务执行时间，意外超时就事务回滚
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock){
                return "请稍后再试";
            }
            // 模拟业务耗时,当前没有显示指定时间，默认时间是30s释放锁,通过查看Redis的key可以看到key是在73s的时候删除的(unlock)
            // 因此可以验证watchDog进行了续期（debug不可以验证，请用耗时操作例如：Thread.sleep）
            Thread.sleep(73000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放最后锁的时间"+LocalDateTime.now());
            lock.unlock();
        }
        return "success";
    }

    /**
     * 这个接口验证watchDog在重入时的生效效果；
     * 可以看到当重入锁释放时value进行了减1的操作，key删除是在finally执行完删除的.
     * 效果是只有业务代码没执行完就不会删除掉锁
     * 请求触发时间: 2021-02-26T20:50:54.180
     * 释放重入锁的时间2021-02-26T20:52:07.201
     * 释放lock的时间2021-02-26T20:53:20.210
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-watch-dog-2")
    public String watchDogReentrantTest(){
        System.out.println("请求触发时间: "+ LocalDateTime.now());
        RLock lock = redissonClient.getLock("watch-dog-test2");
        try {
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock){
                return "请稍后再试";
            }
            boolean reentrantLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!reentrantLock){
                return "请稍后再试,重入锁！";
            }
            Thread.sleep(73000);
            System.out.println("释放重入锁的时间"+LocalDateTime.now());
            lock.unlock();
            Thread.sleep(73000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println("释放lock的时间"+LocalDateTime.now());
            lock.unlock();
        }
        return "success";
    }

    /**
     * 事务处理 事务底层是通过lua实现的
     * 如下代码中 int a= 1/0 这行注释解开就能验证回滚
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-transaction")
    public void transactionTest(){
        RTransaction transaction = redissonClient.createTransaction(TransactionOptions.defaults());
        try {
            RMap<String, String> map = transaction.getMap("transaction-test");
            map.put("123","4");
            map.put("456","5");
            // 开启下面注释就抛异常就会回滚事务
//            int a = 1/0;
            map.put("567","6");
            transaction.commit();
        }catch (Exception e){
            transaction.rollback();
        }
    }
}
