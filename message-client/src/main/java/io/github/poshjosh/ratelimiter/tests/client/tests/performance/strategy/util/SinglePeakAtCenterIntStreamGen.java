package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util;

import java.util.stream.IntStream;

public final class SinglePeakAtCenterIntStreamGen implements IntStreamGenerator {

    private final int [] array;

    public SinglePeakAtCenterIntStreamGen(SinglePeakAtCenterIntStreamConfig config) {

        config.validate();

        array = new int[(config.getPeakPosition() * 2) + 1];

        int lastStepSize = -1;
        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                array[i] = config.getStartValue();
                continue;
            }
            final int stepSize = nextStepSize(config, i, lastStepSize);
            final int value;
            if (i <= config.getPeakPosition()) {
                value = array[i - 1] + stepSize;
            } else {
                value = array[i - 1] - stepSize;
            }
            array[i] = value;
            lastStepSize = stepSize;
        }
    }

    private int nextStepSize(SinglePeakAtCenterIntStreamConfig config, int index, int lastStepSize) {
        if (index == 0) {
            throw new IllegalArgumentException("index must be greater than 0");
        }
        if (index == 1) {
            return config.getStepStartValue();
        }
        if (index <= config.getPeakPosition()) {
            return lastStepSize + config.getStepChange();
        }
        if (index == config.getPeakPosition() + 1) {
            return lastStepSize;
        }
        return lastStepSize - config.getStepChange();
    }

    @Override
    public IntStream stream() {
        return IntStream.of(array);
    }
}
