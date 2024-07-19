package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.IntStreamGenerator;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.SinglePeakAtCenterIntStreamConfig;

import java.util.Arrays;
import java.util.Objects;

public enum RequestSpreadType {

    GAUSSIAN_5
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(false, 5))),

    GAUSSIAN_11
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(false, 11))),

    GAUSSIAN_21
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(false, 21))),
    STEEP_GAUSSIAN_5
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(true, 5))),
    STEEP_GAUSSIAN_11
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(true, 11))),
    STEEP_GAUSSIAN_21
            (singlePeakDescription(SinglePeakAtCenterIntStreamConfig.ofGaussian(true, 21))),
    DYNAMIC("Requests/sec is increased/reduced as memory increases/reduces");

    final String description;

    RequestSpreadType(String description) {
        this.description = Objects.requireNonNull(description);
    }

    @Override public String toString() {
        return description;
    }

    private static String singlePeakSteepGaussianDescription(int size) {
        final int peakPosition = (size - 1) / 2;
        return singlePeakDescription(
                SinglePeakAtCenterIntStreamConfig.ofSteepGaussian(peakPosition));
    }


    private static String singlePeakDescription(SinglePeakAtCenterIntStreamConfig config) {
        final int [] values = IntStreamGenerator.ofSinglePeakAtCenter(config).array();
        return "Values: " + Arrays.toString(values) + ". " + config.getDescription();
    }
}
