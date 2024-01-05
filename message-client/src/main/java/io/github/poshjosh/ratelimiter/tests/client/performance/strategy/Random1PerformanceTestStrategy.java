package io.github.poshjosh.ratelimiter.tests.client.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;
import java.util.Objects;

final class Random1PerformanceTestStrategy implements PerformanceTestStrategy {

    private final TestProcess testProcess;

    Random1PerformanceTestStrategy(TestProcess testProcess) {
        this.testProcess = Objects.requireNonNull(testProcess);
    }

    @Override
    public void run(String id, List<Usage> resultBuffer, double percent) {
        run(id + "_50_1", 50 * percent, resultBuffer);
        for(int i = 0; i < 2; i++) {
            run(id + "_1_1", 1 * percent, resultBuffer);
            run(id + "_5_1", 5 * percent, resultBuffer);
            run(id + "_10_1", 10 * percent, resultBuffer);
            run(id + "_1_2", 1 * percent, resultBuffer);
            run(id + "_5_2", 5 * percent, resultBuffer);
        }
        run(id + "_50_1", 50 * percent, resultBuffer);
    }

    private void run(String id, double requestsPerSecond, List<Usage> resultBuffer) {
        this.testProcess.run(id, requestsPerSecond, resultBuffer);
    }
}
