package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.performance.PerformanceTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UsageController {

    private final Logger log = LoggerFactory.getLogger(UsageController.class);
    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping(value = ResourcePaths.PERFORMANCE_TESTS_PATH, produces = "text/html;charset=UTF-8")
    public String performanceTests(PerformanceTestData performanceTestData) {
        log.debug("#performanceTests({})", performanceTestData);
        return usageService.performanceTests(performanceTestData);
    }

    @GetMapping(ResourcePaths.USAGE_SUMMARY_PATH)
    public ResponseEntity<Map> stats() {
        log.debug("#stats()");
        return usageService.stats();
    }
}