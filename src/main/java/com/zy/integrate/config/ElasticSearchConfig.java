package com.zy.integrate.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangyong
 * Created on 2021-03-01
 */
@EnableConfigurationProperties(ElasticSearchProperties.class)
@Configuration
public class ElasticSearchConfig {
    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Bean(destroyMethod = "close", name = "restHighLevelClient")
    public RestHighLevelClient initRestClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticSearchProperties.getHost(), elasticSearchProperties.getPort()));
        builder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(elasticSearchProperties.getConnTimeout())
                        .setSocketTimeout(elasticSearchProperties.getSocketTimeout())
                        .setConnectionRequestTimeout(elasticSearchProperties.getConnectionRequestTimeout()));
        // TODO 如果有账号密码这块要加上
        return new RestHighLevelClient(builder);
    }
}
