package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.tests.server.model.Usage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UsageRecorder {
    private static final int INTERVAL_SECONDS = 1;
    public static final int INTERVAL_MILLIS = INTERVAL_SECONDS * 1000;
    private static final Object mutex = new Object();
    private static int requestsPerInterval;
    private static int usedMemoryPerInterval;
    private static final List<Usage> usage = new ArrayList<>();

    public void record() {
        synchronized (mutex) {
            ++requestsPerInterval;
            usedMemoryPerInterval += Usage.usedMemory();
        }
    }

    @Scheduled(initialDelay = INTERVAL_MILLIS, fixedRate = INTERVAL_MILLIS)
    public void collect() {
        synchronized (mutex) {
            if (requestsPerInterval < 1) {
                return;
            }
            long requestsPerSecond = requestsPerInterval / INTERVAL_SECONDS;
            long averageMemoryPerRequest = (usedMemoryPerInterval / requestsPerInterval);
            usage.add(Usage.of(requestsPerSecond, Math.max(averageMemoryPerRequest, 0)));
            requestsPerInterval = 0;
            usedMemoryPerInterval = 0;
        }
    }

    public List<Usage> getUsage() {
        synchronized (mutex) {
            return usage;
        }
    }
}
