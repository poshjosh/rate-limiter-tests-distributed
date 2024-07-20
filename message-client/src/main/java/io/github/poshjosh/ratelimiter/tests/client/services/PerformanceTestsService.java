package io.github.poshjosh.ratelimiter.tests.client.services;

import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.resources.ResourcePaths;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.*;
import io.github.poshjosh.ratelimiter.tests.client.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class PerformanceTestsService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestsService.class);

    private final RateLimitMode rateLimitMode;

    private final Path outputDir;

    private final Rest rest;

    private final RateComputer rateComputer;

    public PerformanceTestsService(
            @Value("${app.rate-limit-mode}") String rateLimitModeString,
            @Value("${app.output-dir}") String outputDir,
            Rest rest, RateComputer rateComputer) {
        this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
        this.outputDir = Paths.get(outputDir, "tests", "performance",
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").format(LocalDateTime.now()));
        this.rest = Objects.requireNonNull(rest);
        this.rateComputer = Objects.requireNonNull(rateComputer);
        log.debug("Rate limit mode: {}, output Dir: {}", this.rateLimitMode, this.outputDir);
    }

    public String runPerformanceTests(PerformanceTestData performanceTestData) {
        return new PerformanceTests(
                createRest(performanceTestData),
                performanceTestData,
                createResultHandler(performanceTestData),
                true).run();
    }
    private Rest createRest(PerformanceTestData performanceTestData) {
        final URI uri = ResourcePaths.performanceTestUri(
                rest.createEndpoint(""), performanceTestData.getLimit(),
                performanceTestData.getTimeout(), performanceTestData.getWork());
        return new Rest(uri.toString());
    }

    private PerformanceTestsResultHandler createResultHandler(PerformanceTestData performanceTestData) {
        final String filename = FileUtil.buildFilename(rateLimitMode, performanceTestData);
        return new PerformanceTestsResultHandler(rateComputer, outputDir, filename);
    }
}