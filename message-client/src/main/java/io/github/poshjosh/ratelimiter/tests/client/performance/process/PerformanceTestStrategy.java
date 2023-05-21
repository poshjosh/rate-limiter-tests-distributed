package io.github.poshjosh.ratelimiter.tests.client.performance.process;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;

public interface PerformanceTestStrategy {

    static PerformanceTestStrategy ofSteepGaussian(TestProcess testProcess) {
        return new SteepGaussianPerformanceTestStrategy(testProcess);
    }

    static PerformanceTestStrategy ofGaussian(TestProcess testProcess) {
        return new GaussianPerformanceTestStrategy(testProcess);
    }

    static PerformanceTestStrategy ofRandom1(TestProcess testProcess) {
        return new Random1PerformanceTestStrategy(testProcess);
    }

    static PerformanceTestStrategy ofSteady1(TestProcess testProcess) {
        return new Steady1PerformanceTestStrategy(testProcess);
    }

    void run(String id, List<Thread> threads, List<Usage> resultBuffer, double percent);
}
