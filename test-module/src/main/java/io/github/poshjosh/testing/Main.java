package io.github.poshjosh.testing;

import io.github.poshjosh.ratelimiter.RateLimiter;
import io.github.poshjosh.ratelimiter.RateLimiterFactory;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import io.github.poshjosh.ratelimiter.RateLimiterContext;

import java.time.LocalTime;

public class Main {

    @Rate(id = "classRate", permits = 1)
    static class RateLimitedResource {
        final RateLimiter limiter = RateLimiterFactory
                .getLimiter(RateLimitedResource.class, "methodRate");

        @Rate(id = "methodRate", permits = 2)
        void consume() {
            boolean withinLimit = limiter.tryAcquire();
            System.out.println(LocalTime.now() + " #consume(), within limit: " + withinLimit);
        }
    }

    public static void main(String[] args) {
        System.out.println(LocalTime.now() + " #main(String[])");
        RateLimitedResource resource = new RateLimitedResource();
        for (int i = 0; i < 3; i++) {
            resource.consume();
        }
        RateLimiterContext<String> context = RateLimiterContext.<String>builder()
                .store(BandwidthsStore.<String>ofDefaults())
                .build();
        RateLimiterRegistry<String> registry = RateLimiterRegistry.of(context);
//        RateLimiterFactory<Object> factory = RateLimiterFactory.of(context);
    }
}
