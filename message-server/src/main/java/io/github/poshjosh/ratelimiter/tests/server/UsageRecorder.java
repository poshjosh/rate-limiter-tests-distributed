package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.tests.server.model.Usage;
import io.github.poshjosh.ratelimiter.tests.server.redis.RedisCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsageRecorder {
    private static final int INTERVAL_SECONDS = 1;
    public static final int INTERVAL_MILLIS = INTERVAL_SECONDS * 1000;
    private static final Object mutex = new Object();
    private static int requestsInInterval;
    private static long memoryAtStartOfInterval;
    private static final String LIST_KEY = "UsageRecorder:usageList";

    private final RedisCache<Usage> redisCache;

    public UsageRecorder(@Qualifier("redisUsageCache") RedisCache<Usage> redisCache) {
        this.redisCache = redisCache;
    }

    public void reset() {
        synchronized (mutex) {
            requestsInInterval = 0;
            memoryAtStartOfInterval = 0;
            redisCache.remove(LIST_KEY);
        }
    }

    public void record() {
        synchronized (mutex) {
            ++requestsInInterval;
            if (memoryAtStartOfInterval == 0) {
                memoryAtStartOfInterval = Usage.availableMemory();
            }
        }
    }

    @Scheduled(initialDelay = INTERVAL_MILLIS, fixedRate = INTERVAL_MILLIS)
    public void collect() {
        synchronized (mutex) {
            if (requestsInInterval < 1) {
                return;
            }
            long requestsPerSecond = requestsInInterval / INTERVAL_SECONDS;
            long usedMemoryPerInterval = memoryAtStartOfInterval - Usage.availableMemory();
            long averageMemoryPerRequest = (usedMemoryPerInterval / requestsInInterval);
            Usage usage = Usage.of(requestsPerSecond, Math.max(averageMemoryPerRequest, 0));
            redisCache.addToList(LIST_KEY, usage);
            requestsInInterval = 0;
            memoryAtStartOfInterval = 0;
        }
    }

    public List<Usage> getUsage() {
        synchronized (mutex) {
            return redisCache.getList(LIST_KEY, 0 , 10_000);
        }
    }
}
