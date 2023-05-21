package io.github.poshjosh.ratelimiter.tests.server;

import java.util.Arrays;

public enum RateLimitMode {
    Auto, Manual, Off;

    public static RateLimitMode of(String sval) {
        RateLimitMode[] values = RateLimitMode.values();
        for (RateLimitMode value : values) {
            if (value.name().equalsIgnoreCase(sval)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "Actual: " + sval + ", but expected any of: " + Arrays.toString(values));
    }
}
