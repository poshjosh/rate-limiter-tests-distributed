package io.github.poshjosh.ratelimiter.tests.client;

import java.util.Random;

public final class RandomIps {
    private static final Random random = new Random();
    private RandomIps() { }

    public static String generate(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset = " + offset + ". Must be positive");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit = " + limit + ". Must be greater than zero");
        }
        if ((offset + limit) > 255) {
            throw new IllegalArgumentException("(offset + limit = "
                    + (offset + limit) + "). Must be less than 256");
        }
        return "192.168.168." + (offset + random.nextInt(limit));
    }
}
