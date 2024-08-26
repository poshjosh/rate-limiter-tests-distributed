package io.github.poshjosh.ratelimiter.tests.client.services;

import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.tests.Tests;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.RateLimitMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestsService {
    private final Tests tests;
    private final UsageService usageService;

    public TestsService(
            Rest rest, @Value("${app.rate-limit-mode}") String rateLimitModeString, UsageService usageService) {
        this.tests = new Tests(rest, RateLimitMode.of(rateLimitModeString));
        this.usageService = usageService;
    }

    public String tests() {
        usageService.clearUsageRecord();
        return tests.run();
    }
}
