package io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

final class DynamicPerformanceTestStrategy implements PerformanceTestStrategy {

    private static final Logger log = LoggerFactory.getLogger(DynamicPerformanceTestStrategy.class);

    private final TestProcess testProcess;

    /**
     * The default requests per second. {PerformanceTestStrategy#DEFAULT_REQUESTS_PER_SECOND}
     */
    private final double defaultRequestPerSecond;

    /**
     * The memory of the system will be multiplied by this factor to get the
     * requests per second. The default is 20.
     */
    private final BigDecimal factor;

    private BigDecimal lastMemory;
    private double lastRequestPerSecond;

    DynamicPerformanceTestStrategy(TestProcess testProcess) {
        this(testProcess, PerformanceTestStrategy.DEFAULT_REQUESTS_PER_SECOND, 20);
    }

    DynamicPerformanceTestStrategy(TestProcess testProcess,
            double defaultRequestPerSecond, float factor) {
        this.testProcess = Objects.requireNonNull(testProcess);
        this.defaultRequestPerSecond = defaultRequestPerSecond;
        this.factor = new BigDecimal(String.valueOf(factor));
    }

    @Override
    public void run(String id, List<Usage> resultBuffer, int percent) {

        final int count = Util.computeFactor(percent);

        for(int i = 0; i < count; i++) {

            final double requestsPerSecond = computeRequestsPerSecond(resultBuffer);

            if (requestsPerSecond < 1) {
                break;
            }

            run(id + "_" + i, requestsPerSecond, resultBuffer);
        }
    }

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
            return defaultRequestPerSecond;
        }
        return memory.multiply(factor).intValue();
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
