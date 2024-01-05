package io.github.poshjosh.ratelimiter.tests.client.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.performance.RequestSpreadType;
import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;

public interface PerformanceTestStrategy {

    static PerformanceTestStrategy of(RequestSpreadType requestSpreadType, TestProcess testProcess) {
        switch (requestSpreadType) {
            case DYNAMIC: return ofDynamic(testProcess);
            case STEEP_GAUSSIAN: return ofSteepGaussian(testProcess);
            case RANDOM_1: return ofRandom1(testProcess);
            case STEADY_1: return ofSteady1(testProcess);
            case GAUSSIAN:
            default: return ofGaussian(testProcess);
        }
    }

    static PerformanceTestStrategy ofDynamic(TestProcess testProcess) {
        return new DynamicPerformanceTestStrategy(testProcess);
    }

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

    void run(String id, List<Usage> resultBuffer, double percent);
}
