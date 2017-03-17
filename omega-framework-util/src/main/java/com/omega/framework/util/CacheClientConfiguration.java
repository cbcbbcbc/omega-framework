package com.omega.framework.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by jackychenb on 17/03/2017.
 */
@Configuration
public class CacheClientConfiguration {

    @Bean
    public CacheClient cacheClient(RedisTemplate redisTemplate) {
        return new CacheClient(redisTemplate);
    }

}
