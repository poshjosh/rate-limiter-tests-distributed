package io.github.poshjosh.ratelimiter.tests.server.model;

import java.util.Arrays;

public enum RateLimitMode {
    Auto, Manual, Off, Remote;

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
