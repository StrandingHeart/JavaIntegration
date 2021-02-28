package com.zy.integrate.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author zhangyong05
 * Created on 2021-02-24
 */
@Component
@ConditionalOnProperty(prefix = "redisson",value = "enable", havingValue = "true")
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {

    /**
     * 有 ConfigurationProperties 和 Configuration && Value两种注入配置文件方式
     * ConfigurationProperties 更加方便
     */
    @Autowired
    private RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(redissonProperties.getAddress())
                .setDatabase(redissonProperties.getDatabase())
                .setPassword(redissonProperties.getPassword())
                .setConnectTimeout(redissonProperties.getConnectTimeout())
                .setPingConnectionInterval(redissonProperties.getPingConnectionInterval())
                .setTimeout(redissonProperties.getTimeout());
        return Redisson.create(config);
    }
}
