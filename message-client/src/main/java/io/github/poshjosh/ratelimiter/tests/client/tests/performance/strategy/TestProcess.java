package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.Usage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TestProcess {
    List<CompletableFuture<Usage>> run(String id, double requestPerSec, List<Usage> resultBuffer);
}
