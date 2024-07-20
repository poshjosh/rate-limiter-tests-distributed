package io.github.poshjosh.ratelimiter.tests.client.resources;

import io.github.poshjosh.ratelimiter.tests.client.exception.TestException;
import io.github.poshjosh.ratelimiter.tests.client.services.TestsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestsController {

    private final Logger log = LoggerFactory.getLogger(TestsController.class);

    private final TestsService testsService;

    public TestsController(TestsService testsService) {
        this.testsService = testsService;
    }

    @GetMapping(ResourcePaths.TESTS_PATH)
    public String tests() {
        log.debug("#tests()");
        try {
            return testsService.tests();
        } catch (TestException e) {
            return e.getLocalizedMessage();
        }
    }
}