package io.github.poshjosh.ratelimiter.tests.client.performance.process;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;

import java.util.List;

public interface TestProcess {
    void run(String id, double requestPerSec, List<Thread> threads, List<Usage> resultBuffer);
}
