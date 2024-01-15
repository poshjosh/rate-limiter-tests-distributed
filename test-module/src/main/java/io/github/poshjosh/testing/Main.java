package io.github.poshjosh.testing;

import io.github.poshjosh.ratelimiter.RateLimiter;
import io.github.poshjosh.ratelimiter.RateLimiterFactory;
import io.github.poshjosh.ratelimiter.annotations.Rate;

import java.time.LocalTime;

public class Main {

    @Rate(name = "classRate", permits = 2)
    static class RateLimitedResource {
        final RateLimiter limiter = RateLimiterFactory
                .getLimiter(RateLimitedResource.class, "methodRate");

        @Rate(name = "methodRate", permits = 1)
        void consume() {
            boolean withinLimit = limiter.tryAcquire(1);
            System.out.println(LocalTime.now() + " #consume(), within limit: " + withinLimit);
        }
    }

    public static void main(String[] args) {
        System.out.println(LocalTime.now() + " #main(String[])");
        RateLimitedResource resource = new RateLimitedResource();
        for (int i = 0; i < 3; i++) {
            resource.consume();
        }
    }
}
