package io.github.poshjosh.ratelimiter.tests.server.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class RedisCache<V> {
    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);
    private final RedisTemplate<String, V> redisTemplate;

    public RedisCache(RedisTemplate<String, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public V get(String key) {
        long startTime = System.currentTimeMillis();
        V bandwidth = redisTemplate.opsForValue().get(key);
        long endTime = System.currentTimeMillis();
        log.trace("#get in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
        return bandwidth;
    }

    public void put(String key, V bandwidth) {
        long startTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, bandwidth);
        long endTime = System.currentTimeMillis();
        log.trace("#put in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
    }

    public void addToList(String key, V value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public List<V> getList(String key, int start, int end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Boolean remove(String key) {
        return redisTemplate.delete(key);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
}
