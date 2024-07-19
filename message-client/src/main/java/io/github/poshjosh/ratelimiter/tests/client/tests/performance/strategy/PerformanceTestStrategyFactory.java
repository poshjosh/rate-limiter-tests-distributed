package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.RequestSpreadType;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.IntStreamGenerator;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.SinglePeakAtCenterIntStreamConfig;

final class PerformanceTestStrategyFactory {

    private PerformanceTestStrategyFactory() { }

    public static PerformanceTestStrategy getStrategy(RequestSpreadType requestSpreadType, TestProcess testProcess) {
        switch (requestSpreadType) {
            case DYNAMIC:
                return new DynamicPerformanceTestStrategy(testProcess);
            case STEEP_GAUSSIAN_21: return ofGaussian(testProcess, true, 21);
            case STEEP_GAUSSIAN_11: return ofGaussian(testProcess, true, 11);
            case STEEP_GAUSSIAN_5: return ofGaussian(testProcess, true, 5);
            case GAUSSIAN_21: return ofGaussian(testProcess, false, 21);
            case GAUSSIAN_11: return ofGaussian(testProcess, false, 11);
            case GAUSSIAN_5:
        default:
                return ofGaussian(testProcess,false, 5);
        }
    }

    private static PerformanceTestStrategy ofGaussian(
            TestProcess testProcess, boolean steep, int size) {
        SinglePeakAtCenterIntStreamConfig config =
                SinglePeakAtCenterIntStreamConfig.ofGaussian(steep, size);
        int [] values = IntStreamGenerator.ofSinglePeakAtCenter(config).array();
        return ofCustomValues(testProcess, values);
    }

    private static PerformanceTestStrategy ofCustomValues(
            TestProcess testProcess, int [] values) {
        return new CustomValuesPerformanceTestStrategy(testProcess, values);
    }
}
