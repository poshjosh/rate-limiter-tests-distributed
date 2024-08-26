package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.tests.server.redis.RedisCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RequestCounter {
    private static final String KEY_REQUESTS = "RequestCounter:requests";
    private static final String KEY_REJECTIONS = "RequestCounter:rejections";

    private final RedisCache<Long> redisLongCache;

    public RequestCounter(@Qualifier("redisLongCache") RedisCache<Long> redisLongCache) {
        this.redisLongCache = Objects.requireNonNull(redisLongCache);
    }

    public void incrementRequests() { redisLongCache.increment(KEY_REQUESTS); }
    public void incrementRejections() { redisLongCache.increment(KEY_REJECTIONS); }

    public long getRequests() { return redisLongCache.increment(KEY_REQUESTS) - 1; }
    public long getRejections() {
        return redisLongCache.increment(KEY_REJECTIONS) - 1;
    }
}
