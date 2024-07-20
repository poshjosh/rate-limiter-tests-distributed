package io.github.poshjosh.ratelimiter.tests.client.resources;

import io.github.poshjosh.ratelimiter.tests.client.exception.TestException;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.PerformanceTestData;
import io.github.poshjosh.ratelimiter.tests.client.services.PerformanceTestsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceTestsController {

    private final Logger log = LoggerFactory.getLogger(PerformanceTestsController.class);
    private final PerformanceTestsService performanceTestsService;

    public PerformanceTestsController(PerformanceTestsService performanceTestsService) {
        this.performanceTestsService = performanceTestsService;
    }

    @RequestMapping(
            method = {RequestMethod.GET, RequestMethod.POST},
            value = ResourcePaths.PERFORMANCE_TESTS_PATH,
            produces = "text/html;charset=UTF-8")
    public String performanceTests(PerformanceTestData performanceTestData) {
        log.debug("#performanceTests({})", performanceTestData);
        try {
            return performanceTestsService.runPerformanceTests(performanceTestData);
        } catch (TestException e) {
            return e.getLocalizedMessage();
        }
    }
}