package com.omega.framework.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by jackychenb on 12/12/2016.
 */

public class RedisCacheClient implements ICacheClient {

    private final RedisTemplate redisTemplate;
    public RedisCacheClient(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(final String key, final Object value) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        public void afterCommit() {
                            redisTemplate.opsForValue().set(key, value);
                        }
                    });
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public void set(final String key, final Object value, final long lifecycle) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        public void afterCommit() {
                            redisTemplate.opsForValue().set(key, value, lifecycle, TimeUnit.MILLISECONDS);
                        }
                    });
        } else {
            redisTemplate.opsForValue().set(key, value, lifecycle, TimeUnit.MILLISECONDS);
        }
    }

    public Object get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void remove(final String key) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        public void afterCommit() {
                            redisTemplate.delete(key);
                        }
                    });
        } else {
            redisTemplate.delete(key);
        }
    }

}
