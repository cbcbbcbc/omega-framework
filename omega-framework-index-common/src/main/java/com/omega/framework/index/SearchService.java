package com.omega.framework.index;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created by jackychenb on 13/03/2017.
 */
public class SearchService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CuratorFramework curatorFramework;

    @Autowired
    private TransportClient elasticsearchClient;

    @Value("${elasticsearch.refresh.lockPath:/index/refresh}")
    private String lockPath = "/index/refresh";

    @Value("${elasticsearch.refresh.duration:500}")
    private long refreshDuration = 500L; // 预估的最长的refresh调用耗时，单位毫秒

    private String getCacheKey(String indexName) {
        return indexName;
    }

    private String getLockName(String indexName) {
        return lockPath + "/" + indexName;
    }

    public void ensureRefresh(String indexName, String lastCommandId) throws Exception {
        Long submitTime = (Long) redisTemplate.opsForValue().get(lastCommandId);
        if (submitTime == null) {
            return;
        }

        String key = getCacheKey(indexName);
        Long refreshTime = (Long) redisTemplate.opsForValue().get(key);
        if (refreshTime != null && submitTime.compareTo(refreshTime) < 0) {
            return;
        }

        String lockName = getLockName(indexName);
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, lockName);

        try {
            if (lock.acquire(refreshDuration, TimeUnit.MILLISECONDS)) {
                // 获得锁后再次检查索引库更新时间
                Long refreshTime2 = (Long) redisTemplate.opsForValue().get(key);
                if (refreshTime2 != null && submitTime.compareTo(refreshTime2) < 0) {
                    return;
                }

                // 时间戳一定要在调用ES刷新接口前获得
                long now = System.currentTimeMillis();
                elasticsearchClient.admin().indices().prepareRefresh(indexName).get();
                redisTemplate.opsForValue().set(key, now);
            }
        } finally {
            lock.release();
        }
    }

}
