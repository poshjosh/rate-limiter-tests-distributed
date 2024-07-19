package io.github.poshjosh.ratelimiter.tests.client.services;

import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.resources.ResourcePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Service
public class UsageService {

    private final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final Rest rest;

    public UsageService(Rest rest) {
        this.rest = rest;
    }

    public ResponseEntity<Map> stats() {
        log.debug("#stats()");
        Function<RestClientException, Map> onError = e -> Collections.singletonMap("error", e.toString());
        return rest.get(ResourcePaths.USAGE_SUMMARY_PATH, Map.class, onError);
    }
}