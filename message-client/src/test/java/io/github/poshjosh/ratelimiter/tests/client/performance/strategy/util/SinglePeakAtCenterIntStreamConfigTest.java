package io.github.poshjosh.ratelimiter.tests.client.performance.strategy.util;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.util.SinglePeakAtCenterIntStreamConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SinglePeakAtCenterIntStreamConfigTest {

    @ParameterizedTest
    @CsvSource({"0, 7, 7, 7", "7, -1, 7, 7", "7, 7, -1, 7", "7, 7, 7, 0"})
    void testValidateThrowsExceptionGivenInvalidValues(
            int startValue, int stepStartValue, int stepChange, int peakPosition) {
        SinglePeakAtCenterIntStreamConfig config = new SinglePeakAtCenterIntStreamConfig(
                startValue, stepStartValue, stepChange, peakPosition);
        assertThrows(IllegalArgumentException.class, config::validate);
    }

    @ParameterizedTest
    @CsvSource({"1, 7, 7, 7", "7, 0, 7, 7", "7, 7, 0, 7", "7, 7, 7, 1"})
    void testValidateIsSuccessfulGivenValidValues(
            int startValue, int stepStartValue, int stepChange, int peakPosition) {
        SinglePeakAtCenterIntStreamConfig config = new SinglePeakAtCenterIntStreamConfig(
                startValue, stepStartValue, stepChange, peakPosition);
        assertDoesNotThrow(config::validate);
    }
}