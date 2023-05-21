package io.github.poshjosh.ratelimiter.tests.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsageService {
    private static final BigDecimal ONE_MILLION = new BigDecimal(1_000_000); // Could also be 1024 * 1024
    private final MessageServer.RateLimitingFilter rateLimitingFilter;
    private final UsageRecorder usageRecorder;
    private final RateLimitMode rateLimitMode;

    public UsageService(
            MessageServer.RateLimitingFilter rateLimitingFilter,
            UsageRecorder usageRecorder,
            @Value("${app.rate-limit-mode}") String rateLimitModeString) {
        this.rateLimitingFilter = rateLimitingFilter;
        this.usageRecorder = usageRecorder;
        this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
    }

    public BigDecimal work(int work) {
        usageRecorder.record();
        RandomWorkSimulator.simulateSomeWork(work * 1_000);
        return toMegaBytes(Usage.usedMemory());
    }

    public List<Usage> usage() {
        return usageRecorder.getUsage();
    }

    public Map<String, Object> summary() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("jvm_available_memory", Usage.ByteText.of(Usage.availableMemory()));
        stats.put("jvm_max_memory", Usage.ByteText.of(Runtime.getRuntime().maxMemory()));
        stats.put("jvm_used_memory", Usage.ByteText.of(Usage.usedMemory()));
        stats.put("rate_limit_consumption", rateLimitingFilter.getConsumption());
        stats.put("rate_limit_mode", rateLimitMode);
        stats.put("rate_limit_rejection", rateLimitingFilter.getRejection());
        stats.put("requests", rateLimitingFilter.getRequests());
        return stats;
    }

    private BigDecimal toMegaBytes(long dividend) {
        return BigDecimal.valueOf(dividend).divide(ONE_MILLION,2, RoundingMode.CEILING);
    }
}
