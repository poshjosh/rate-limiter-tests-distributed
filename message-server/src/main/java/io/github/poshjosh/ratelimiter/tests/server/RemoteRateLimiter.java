package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.client.RateLimiterServiceClient;
import io.github.poshjosh.ratelimiter.client.ServerException;
import io.github.poshjosh.ratelimiter.tests.server.model.RateLimitMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RemoteRateLimiter {
    private final RateLimiterServiceClient postClient;
    private final RateLimiterServiceClient fastClient;
    private final boolean enabled;

    public RemoteRateLimiter(
            @Value("${app.rate-limit-mode}") String rateLimitModeString,
            @Value("${app.rate-limiter.service.url}") String rateLimiterServiceUrl,
            @Value("${app.rate-limiter.service.timeout-millis}") long timeoutMillis) {
        this.postClient = new RateLimiterServiceClient(rateLimiterServiceUrl);
        this.fastClient = postClient.withTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        this.enabled = RateLimitMode.Remote == RateLimitMode.of(rateLimitModeString);
    }

    public void addRate(String id, String rate, String condition) {
        if (!enabled) { return; }
        try {
            postClient.postRate(id, rate, condition);
        } catch (IOException | ServerException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkLimit(String id, HttpServletRequest request) {
        if (!enabled) { return; }
        if (!fastClient.tryToAcquirePermitQuietly(id, request)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
