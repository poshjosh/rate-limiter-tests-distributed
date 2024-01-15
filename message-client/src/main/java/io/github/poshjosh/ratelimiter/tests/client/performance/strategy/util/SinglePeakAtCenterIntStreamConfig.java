package io.github.poshjosh.ratelimiter.tests.client.performance.strategy.util;

public final class SinglePeakAtCenterIntStreamConfig {

    public static SinglePeakAtCenterIntStreamConfig ofGaussian(boolean steep, int size) {
        final int peakPosition = (size - 1) / 2;
        return steep ? ofSteepGaussian(peakPosition) : ofGaussian(peakPosition);
    }

    public static SinglePeakAtCenterIntStreamConfig ofGaussian(int peakPosition) {
        return new SinglePeakAtCenterIntStreamConfig(1, 0, 5, peakPosition);
    }

    public static SinglePeakAtCenterIntStreamConfig ofSteepGaussian(int peakPosition) {
        return new SinglePeakAtCenterIntStreamConfig(1, 10, 90, peakPosition);
    }

    private final int startValue;
    private final int stepStartValue;
    private final int stepChange;
    private final int peakPosition;
    public SinglePeakAtCenterIntStreamConfig(
            int startValue, int stepStartValue, int stepChange, int peakPosition) {
        this.startValue = startValue;
        this.stepStartValue = stepStartValue;
        this.stepChange = stepChange;
        this.peakPosition = peakPosition;
    }

    public void validate() {
        if (startValue <= 0) {
            throw new IllegalArgumentException("startValue cannot be <= 0");
        }
        if (stepStartValue < 0) {
            throw new IllegalArgumentException("stepStartValue cannot be < 0");
        }
        if (stepChange < 0) {
            throw new IllegalArgumentException("stepChange cannot be < 0");
        }
        if (peakPosition <= 0) {
            throw new IllegalArgumentException("peakPosition cannot be 0");
        }
    }

    public int getStartValue() {
        return startValue;
    }

    public int getStepStartValue() {
        return stepStartValue;
    }

    public int getStepChange() {
        return stepChange;
    }

    public int getPeakPosition() {
        return peakPosition;
    }

    public String getDescription() {
        return "Requests/sec increases from " + startValue +
                " in steps (starting from " + stepStartValue +
                " and changing at a rate +" + stepChange +
                ") up to a peak at position " + peakPosition +
                " and then decreases in steps, (now changing at a rate -" + stepChange +
                "), down to the start value.";
    }

    @Override
    public String toString() {
        return "SinglePeakAtCenterIntStreamConfig{" + "startValue=" + startValue
                + ", stepStartValue=" + stepStartValue + ", stepChange=" + stepChange
                + ", peakPosition=" + peakPosition + '}';
    }
}
