package io.github.poshjosh.ratelimiter.tests.client.services;

import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.tests.Tests;
import org.springframework.stereotype.Service;

@Service
public class TestsService {
    private final Tests tests;

    public TestsService(Rest rest) {
        this.tests = new Tests(rest);
    }

    public String tests() {
        return tests.run();
    }
}
