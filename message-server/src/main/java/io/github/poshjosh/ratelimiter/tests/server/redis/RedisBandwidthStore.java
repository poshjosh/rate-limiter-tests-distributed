package io.github.poshjosh.ratelimiter.tests.server.redis;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisBandwidthStore implements BandwidthsStore<String> {
    private static final Logger log = LoggerFactory.getLogger(RedisBandwidthStore.class);
    private static final ThreadLocal<String> lastGottenKey = new ThreadLocal<>();

    private static final ThreadLocal<String> lastPutKey = new ThreadLocal<>();

    private final RedisTemplate<String, Bandwidth> redisTemplate;

    public RedisBandwidthStore(RedisTemplate<String, Bandwidth> redisTemplate) {
        this.redisTemplate = redisTemplate;
        final Runnable clearThreadLocals = () -> {
            lastGottenKey.remove();
            lastPutKey.remove();
        };
        Runtime.getRuntime().addShutdownHook(new Thread(clearThreadLocals));
    }

    public static String getLastGottenKey() {
        return lastGottenKey.get();
    }

    public static String getLastPutKey() {
        return lastPutKey.get();
    }

    @Override
    public Bandwidth get(String key) {
        long startTime = System.currentTimeMillis();
        Bandwidth bandwidth = redisTemplate.opsForValue().get(key);
        long endTime = System.currentTimeMillis();
        lastGottenKey.set(key);
        log.trace("#get in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
        return bandwidth;
    }

    @Override
    public void put(String key, Bandwidth bandwidth) {
        long startTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, bandwidth);
        long endTime = System.currentTimeMillis();
        lastPutKey.set(key);
        log.trace("#put in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
    }
}
