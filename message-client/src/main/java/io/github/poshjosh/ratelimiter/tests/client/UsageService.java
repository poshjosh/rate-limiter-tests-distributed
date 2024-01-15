package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.performance.*;
import io.github.poshjosh.ratelimiter.tests.client.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Service
public class UsageService {

    private final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final RateLimitMode rateLimitMode;

    private final Path outputDir;

    private final Rest rest;

    private final RateComputer rateComputer;

    public UsageService(
            @Value("${app.rate-limit-mode}") String rateLimitModeString,
            @Value("${app.output-dir}") String outputDir,
            Rest rest, RateComputer rateComputer) {
        this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
        this.outputDir = Paths.get(outputDir);
        this.rest = rest;
        this.rateComputer = rateComputer;
        log.info("Rate limit mode: {}, output Dir: {}", this.rateLimitMode, this.outputDir);
    }

    public String performanceTests(PerformanceTestData performanceTestData) {
        return new PerformanceTests(
                rest.createServerUri(""),
                performanceTestData,
                createResultHandler(performanceTestData)).run();
    }

    private PerformanceTestsResultHandler createResultHandler(PerformanceTestData performanceTestData) {
        final String filename = FileUtil.buildFilename(rateLimitMode, performanceTestData);
        return new PerformanceTestsResultHandler(rateComputer, outputDir, filename);
    }

    public ResponseEntity<Map> stats() {
        log.debug("#stats()");
        Function<RestClientException, Map> onError = e -> Collections.singletonMap("error", e.toString());
        return rest.getFromServer(ResourcePaths.USAGE_SUMMARY_PATH, Map.class, onError);
    }
}