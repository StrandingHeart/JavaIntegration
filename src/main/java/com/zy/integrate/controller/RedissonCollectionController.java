package com.zy.integrate.controller;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangyong05
 * Created on 2021-02-25
 */
@RestController
public class RedissonCollectionController {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 对应Redis中的hash 还有cache以及local以及multi多种操作方式。但是我觉得使用场景并不多就没写demo
     * cache是给value加了 ttl
     * local是加了本地缓存
     * multi是提供多操作，Java对象允许Map中的一个字段值包含多个元素。
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-map")
    public void mapTest(){
        // 对应 HSET field value
        RMap<String, String> map = redissonClient.getMap("map-test");
        map.putIfAbsent("field","value1");
        map.put("123","123qwe");
        map.fastPut("123","456rty");

        // mapCache 可以设置value的消失时间ttl 以及 最长闲置时间 挺鸡肋的,感觉作用不大，性能下滑。
        RMapCache<String, String> mapCache = redissonClient.getMapCache("cache-map-test");
        mapCache.put("sad","eqw",1, TimeUnit.HOURS,1,TimeUnit.MINUTES);
    }

    /**
     * 对应Redis中的set，可以排序
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-set")
    public void setTest(){
        RSet<String> set = redissonClient.getSet("set-test");
        set.add("asd");
        set.add("123");
        set.add("456");
        set.add("789");
        set.remove("456");

        // set也可以用cache 还有批量操作，但是感觉这种功能使用场景比较少。
        // 排序的sort 可以指定comparator
        RSortedSet<Integer> sortedSet = redissonClient.getSortedSet("sort-set-test");
        sortedSet.add(6);
        sortedSet.add(3);
        sortedSet.add(1);
        sortedSet.add(7);
    }

    /**
     * 带有score的zset操作
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-zset")
    public void setScoreTest(){
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet("score-set-test");
        scoredSortedSet.add(90.22,"数学");
        scoredSortedSet.add(98.22,"语文");
        scoredSortedSet.add(92.22,"英语");
        // 相同的覆盖，最后英语为93.22
        scoredSortedSet.add(93.22,"英语");
        Double score = scoredSortedSet.getScore("数学");
        System.out.println(score);
        // rank 从0起始
        Integer rank = scoredSortedSet.rank("数学");
        System.out.println(rank);
    }

    /**
     * 就是一个放到Redis中的队列  list
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-queue")
    public void queueTest(){
        // 无界队列
        RQueue<String> queue = redissonClient.getQueue("queue-test");
        // queue 中使用offer和poll 做入队和出队的操作。poll会删除掉队首元素
        queue.offer("sad");
        queue.offer("wqe");
        queue.offer("123");
        queue.offer("456");
        queue.poll();
    }

    /**
     * 有界队列 list 这个可以作为过滤进几次筛选结果的需求等等
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-bound-queue")
    public void boundBlockQueue(){
        // 使用场景过滤近几次筛选过的结果
        RBoundedBlockingQueue<Long> boundedBlockingQueue = redissonClient.getBoundedBlockingQueue("bound-queue-test");
        // 设置有界队列的长度为2
        int bound = 2;
        boundedBlockingQueue.trySetCapacity(bound);
        // offer操作，当队列满时就不加了；判断队列满的话先出队再入队；
        if (boundedBlockingQueue.size() == bound){
            boundedBlockingQueue.poll();
        }
        // 可以验证Redis的值
        boundedBlockingQueue.offer(System.currentTimeMillis());
    }

    /**
     * 优先队列，list 这个也可以作为不断取最高(低)的 这种需求
     * @param
     * @return
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-priority-queue")
    public void priorityQueueTest(){
        // 优先队列，可以设置comparator
        RPriorityQueue<Integer> priorityQueue = redissonClient.getPriorityQueue("priority-queue-test");
        priorityQueue.offer(3);
        priorityQueue.offer(1);
        priorityQueue.offer(4);
        System.out.println(priorityQueue.poll());
    }

    /**
     * 双端优先队列
     * @author zhangyong05
     * 2021/2/26
     */
    @GetMapping("/redisson-priority-deque")
    public void priorityDequeTest(){
        // LIST
        RPriorityDeque<Integer> priorityDeque = redissonClient.getPriorityDeque("priority-deque-test");
        priorityDeque.add(3);
        priorityDeque.add(1);
        priorityDeque.add(4);
        // 当队列没有数据时，poll操作不会报错 如果触发进俩出去俩，队列为空，Redis不展示这个key
        priorityDeque.pollLast();
        priorityDeque.pollFirst();
    }

}
