package io.github.poshjosh.ratelimiter.tests.client.performance.process;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;
import java.util.Objects;

final class SteepGaussianPerformanceTestStrategy implements PerformanceTestStrategy {

    private final TestProcess testProcess;

    SteepGaussianPerformanceTestStrategy(TestProcess testProcess) {
        this.testProcess = Objects.requireNonNull(testProcess);
    }

    @Override
    public void run(String id, List<Thread> threads, List<Usage> resultBuffer, double percent) {
        run(id + "_1_1", 1 * percent, threads, resultBuffer);
        run(id + "_10_1", 10 * percent, threads, resultBuffer);
        run(id + "_100", 100 * percent, threads, resultBuffer);
        run(id + "_10_2", 10 * percent, threads, resultBuffer);
        run(id + "_1_2", 1 * percent, threads, resultBuffer);
    }

    private void run(String id, double requestsPerSecond, List<Thread> threads, List<Usage> resultBuffer) {
        this.testProcess.run(id, requestsPerSecond, threads, resultBuffer);
    }
}
