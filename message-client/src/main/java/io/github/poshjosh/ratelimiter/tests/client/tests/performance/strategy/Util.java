package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy;

final class Util {
    private Util() {}
    static int computeFactor(int percent) {
        final int factor = (int)((double)percent / 100);
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be < 0: " + factor);
        }
        return factor;
    }
}
