package io.github.poshjosh.ratelimiter.tests.server.services;

import io.github.poshjosh.ratelimiter.tests.server.model.RateLimitMode;
import io.github.poshjosh.ratelimiter.tests.server.model.Usage;
import io.github.poshjosh.ratelimiter.tests.server.UsageRecorder;
import io.github.poshjosh.ratelimiter.tests.server.RequestCounter;
import io.github.poshjosh.ratelimiter.tests.server.util.RandomWorkSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsageService {

    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private static final BigDecimal ONE_MILLION = new BigDecimal(1_000_000); // Could also be 1024 * 1024
    private final RequestCounter requestCounter;
    private final UsageRecorder usageRecorder;
    private final RateLimitMode rateLimitMode;

    public UsageService(
            UsageRecorder usageRecorder,
            @Value("${app.rate-limit-mode}") String rateLimitModeString,
            RequestCounter requestCounter) {
        this.usageRecorder = usageRecorder;
        this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
        this.requestCounter = requestCounter;
    }

    public BigDecimal work(int work) {
        usageRecorder.record();
        RandomWorkSimulator.simulateSomeWork(work * 1_000);
        return toMegaBytes(Usage.usedMemory());
    }

    public void clearUsageRecord() {
        usageRecorder.reset();
    }

    public List<Usage> usage() {
        return usageRecorder.getUsage();
    }

    public Map<String, Object> summary() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("JVM available memory", Usage.ByteText.of(Usage.availableMemory()));
        stats.put("JVM max memory", Usage.ByteText.of(Usage.maxMemory()));
        stats.put("JVM used memory", Usage.ByteText.of(Usage.usedMemory()));
        stats.put("Rate limit mode", rateLimitMode);
        stats.put("Total number of requests", requestCounter.getRequests());
        stats.put("Requests rejected by rate limiter", requestCounter.getRejections());
        log.debug("Summary: {}", stats);
        return stats;
    }

    private static BigDecimal toMegaBytes(long dividend) {
        return BigDecimal.valueOf(dividend).divide(ONE_MILLION,2, RoundingMode.CEILING);
    }
}
