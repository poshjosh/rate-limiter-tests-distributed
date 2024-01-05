package io.github.poshjosh.ratelimiter.tests.client.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.performance.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

final class DynamicPerformanceTestStrategy implements PerformanceTestStrategy {

    private static final Logger log = LoggerFactory.getLogger(DynamicPerformanceTestStrategy.class);

    private final TestProcess testProcess;

    private BigDecimal lastMemory;
    private double lastRequestPerSecond;

    DynamicPerformanceTestStrategy(TestProcess testProcess) {
        this.testProcess = Objects.requireNonNull(testProcess);
    }

    @Override
    public void run(String id, List<Usage> resultBuffer, double percent) {

        final int count = (int)(100 * percent);

        for(int i = 0; i < count; i++) {

            final double requestsPerSecond = computeRequestsPerSecond(resultBuffer);

            if (requestsPerSecond < 1) {
                break;
            }

            run(id + "_" + i, requestsPerSecond, resultBuffer);
        }
    }

    private static final BigDecimal LEVEL_1 = new BigDecimal(100);
    private static final BigDecimal LEVEL_2 = new BigDecimal(300);
    private static final BigDecimal LEVEL_3 = new BigDecimal(500);
    private static final BigDecimal LEVEL_4 = new BigDecimal(700);
    private static final BigDecimal LEVEL_5 = new BigDecimal(900);

    private double computeRequestsPerSecond(List<Usage> resultBuffer) {

        final BigDecimal memory = lastMemory(resultBuffer);
        final double requestsPerSec = computeRequestsPerSecond(memory);

        log.info("Last: rps=" + lastRequestPerSecond + ", mem=" + lastMemory +
                ", Curr: rps=" + requestsPerSec + ", mem=" + memory);

        lastRequestPerSecond = requestsPerSec;
        lastMemory = memory;

        return requestsPerSec;
    }

    private double computeRequestsPerSecond(BigDecimal memory) {
        if (memory == null) {
            return 5;
        }

        if (memory.compareTo(BigDecimal.ZERO) <= 0) {
            return 60;
        }

        if (memory.compareTo(LEVEL_1) <= 0) {
            return 50;
        }

        if (memory.compareTo(LEVEL_2) <= 0) {
            return 40; // 30
        }

        if (memory.compareTo(LEVEL_3) <= 0) {
            return 30; // 20
        }

        if (memory.compareTo(LEVEL_4) <= 0) {
            return 20; //5
        }

        if (memory.compareTo(LEVEL_5) <= 0) {
            return 5; //1
        }

        return 0;
    }

    private BigDecimal lastMemory(List<Usage> resultBuffer) {
        if (resultBuffer.isEmpty()) {
            return null;
        }
        Usage usage = resultBuffer.get(resultBuffer.size() - 1);
        return usage.getMemory();
    }

    private void run(String id, double requestsPerSecond, List<Usage> resultBuffer) {
        this.testProcess.run(id, requestsPerSecond, resultBuffer);
    }
}
