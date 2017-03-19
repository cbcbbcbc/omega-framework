package com.omega.framework.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by jackychenb on 19/03/2017.
 */

@Component
public class IndexWorkerHelper {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${elasticsearch.refresh.interval:30000}")
    private long refreshInterval = 30000;

    @Value("${elasticsearch.refresh.duration:500}")
    private long refreshDuration = 500L; // 预估的最长的refresh调用耗时

    private String getCommandCacheKey(String commandId) {
        return "index/" + commandId;
    }

    private String getIndexCacheKey(String indexName) {
        return indexName + "/index";
    }

    public void saveCommandFinishTime(String cmdId, long time) {
        long lifecycle = refreshInterval + refreshDuration;
        redisTemplate.opsForValue().set(getCommandCacheKey(cmdId), time, lifecycle, TimeUnit.MILLISECONDS);
    }

    public void saveLastCommandFinishTime(String indexName, long time) {
        long lifecycle = refreshInterval + refreshDuration;
        redisTemplate.opsForValue().set(getIndexCacheKey(indexName), time, lifecycle, TimeUnit.MILLISECONDS);
    }

    public Long getCommandFinishTime(String cmdId) {
        return (Long) redisTemplate.opsForValue().get(getCommandCacheKey(cmdId));
    }

    public Long getLastCommandFinishTime(String indexName) {
        return (Long) redisTemplate.opsForValue().get(getIndexCacheKey(indexName));
    }

}
