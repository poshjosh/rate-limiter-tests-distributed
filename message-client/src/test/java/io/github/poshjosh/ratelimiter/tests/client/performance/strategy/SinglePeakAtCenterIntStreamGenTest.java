package io.github.poshjosh.ratelimiter.tests.client.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.IntStreamGenerator;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.SinglePeakAtCenterIntStreamConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SinglePeakAtCenterIntStreamGenTest {

    @Test
    void singlePeakAtCenterShouldSucceed() {
        int [] result = getIntStreamGen(1000, 100, 10, 2).array();
        assertArrayEquals(new int[]{1000, 1100, 1210, 1100, 1000}, result);
    }

    @Test
    void singlePeakAtCenterShouldSucceedGivenZeroStepChange() {
        int [] result = getIntStreamGen(1, 1, 0, 2).array();
        assertArrayEquals(new int[]{1, 2, 3, 2, 1}, result);
    }

    @Test
    void singlePeakAtCenterShouldSucceedGivenZeroStepChangeAndLargeStart() {
        int [] result = getIntStreamGen(1_000_000, 10, 0, 2).array();
        assertArrayEquals(new int[]{1_000_000, 1_000_010, 1_000_020, 1_000_010, 1_000_000}, result);
    }

    private IntStreamGenerator getIntStreamGen(
            int startValue, int stepStartValue, int stepChange, int peakPosition) {
        SinglePeakAtCenterIntStreamConfig config = new SinglePeakAtCenterIntStreamConfig(
                startValue, stepStartValue, stepChange, peakPosition);
        return IntStreamGenerator.ofSinglePeakAtCenter(config);
    }
}