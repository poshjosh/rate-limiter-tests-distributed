package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.RequestSpreadType;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.Usage;

import java.util.List;

public interface PerformanceTestStrategy {

    int DEFAULT_REQUESTS_PER_SECOND = 10;

    static PerformanceTestStrategy of(RequestSpreadType requestSpreadType, TestProcess testProcess) {
        return PerformanceTestStrategyFactory.getStrategy(requestSpreadType, testProcess);
    }

    void run(String id, List<Usage> resultBuffer, int percent);
}
