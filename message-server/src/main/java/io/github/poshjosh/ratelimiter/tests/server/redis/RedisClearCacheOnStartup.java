package io.github.poshjosh.ratelimiter.tests.server.redis;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@Configuration
public class RedisClearCacheOnStartup {
    private static final Logger log = LoggerFactory.getLogger(RedisClearCacheOnStartup.class);

    RedisClearCacheOnStartup(
            // Any template would do, we just need to clear the cache
            @Qualifier("redisBandwidthTemplate") RedisTemplate<String, Bandwidth> redisTemplate) {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            log.info("=========================== DELETING Cache ===========================");
            keys.forEach(key -> log.info("Deleted: {}, {}", redisTemplate.delete(key), key));
            log.info("======================================================================");
        }
    }
}