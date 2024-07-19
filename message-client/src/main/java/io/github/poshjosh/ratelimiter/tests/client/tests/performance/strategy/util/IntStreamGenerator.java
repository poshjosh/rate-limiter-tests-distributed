package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util;

import java.util.stream.IntStream;

public interface IntStreamGenerator {
    static IntStreamGenerator ofSinglePeakAtCenter(SinglePeakAtCenterIntStreamConfig config) {
        return new SinglePeakAtCenterIntStreamGen(config);
    }
    default int [] array() {
        return stream().toArray();
    }
    IntStream stream();
}
