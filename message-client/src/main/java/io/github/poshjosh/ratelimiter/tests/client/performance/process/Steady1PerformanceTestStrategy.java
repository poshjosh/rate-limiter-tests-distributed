package io.github.poshjosh.ratelimiter.tests.client.performance.process;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;
import java.util.Objects;

final class Steady1PerformanceTestStrategy implements PerformanceTestStrategy {

    private final TestProcess testProcess;

    Steady1PerformanceTestStrategy(TestProcess testProcess) {
        this.testProcess = Objects.requireNonNull(testProcess);
    }

    @Override
    public void run(String id, List<Thread> threads, List<Usage> resultBuffer, double percent) {
        for(int i = 0; i < 2; i++) {
            run(id + '_' + i + "_50_1", 50 * percent, threads, resultBuffer);
            run(id + '_' + i + "_5_1", 5 * percent, threads, resultBuffer);
            run(id + '_' + i + "_5_2", 5 * percent, threads, resultBuffer);
            run(id + '_' + i + "_5_3", 5 * percent, threads, resultBuffer);
            run(id + '_' + i + "_10_1", 10 * percent, threads, resultBuffer);
            run(id + '_' + i + "_10_2", 10 * percent, threads, resultBuffer);
            run(id + '_' + i + "_10_3", 10 * percent, threads, resultBuffer);
        }
    }

    private void run(String id, double requestsPerSecond, List<Thread> threads, List<Usage> resultBuffer) {
        this.testProcess.run(id, requestsPerSecond, threads, resultBuffer);
    }
}
