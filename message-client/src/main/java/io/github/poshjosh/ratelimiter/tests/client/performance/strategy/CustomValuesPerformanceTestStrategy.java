package io.github.poshjosh.ratelimiter.tests.client.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;
import java.util.Objects;

final class CustomValuesPerformanceTestStrategy implements PerformanceTestStrategy {

    private final TestProcess testProcess;
    private final int [] values;

    CustomValuesPerformanceTestStrategy(TestProcess testProcess, int [] values) {
        this.testProcess = Objects.requireNonNull(testProcess);
        this.values = Objects.requireNonNull(values);
    }

    @Override
    public void run(String id, List<Usage> resultBuffer, int percent) {
        final int factor = Util.computeFactor(percent);
        for(int i = 0; i < values.length; i++) {
            run(id + "_" + i + "_", values[i] * factor, resultBuffer);
        }
    }

    private void run(String id, double requestsPerSecond, List<Usage> resultBuffer) {
        this.testProcess.run(id, requestsPerSecond, resultBuffer);
    }
}
