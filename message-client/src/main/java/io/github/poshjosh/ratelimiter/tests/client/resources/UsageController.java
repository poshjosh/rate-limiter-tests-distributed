package io.github.poshjosh.ratelimiter.tests.client.resources;

import io.github.poshjosh.ratelimiter.tests.client.services.UsageService;
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

    @GetMapping(ResourcePaths.USAGE_SUMMARY_PATH)
    public ResponseEntity<Map> stats() {
        log.debug("#stats()");
        return usageService.stats();
    }
}