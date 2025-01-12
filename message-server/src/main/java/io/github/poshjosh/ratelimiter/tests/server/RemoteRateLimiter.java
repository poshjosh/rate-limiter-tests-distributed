package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.client.RateLimiterClient;
import io.github.poshjosh.ratelimiter.tests.server.model.RateLimitMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Component
public class RemoteRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RemoteRateLimiter.class);

    private final boolean enabled;
    private final RateLimiterClient rateLimiterClient;
    private final RequestCounter requestCounter;

    public RemoteRateLimiter(
            @Value("${app.rate-limit-mode}") String rateLimitModeString,
            @Value("${app.rate-limiter.service.url}") String rateLimiterServiceUrl,
            @Value("${app.rate-limiter.service.timeout-millis}") long timeoutMillis,
            RequestCounter requestCounter) {
        log.info("Will connect to remote rate limiter service: {}", rateLimiterServiceUrl);
        this.rateLimiterClient =
                new RateLimiterClient(rateLimiterServiceUrl)
                        .withTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        this.enabled = RateLimitMode.Remote == RateLimitMode.of(rateLimitModeString);
        this.requestCounter = requestCounter;
    }

    public void checkLimit(HttpServletRequest request, String id, String rate) {
        checkLimit(request, id, rate, null);
    }

    public void checkLimit(HttpServletRequest request, String id, String rate, String condition) {
        if (!enabled) { return; }
        if (!rateLimiterClient.isWithinLimit(request, id, rate, condition)) {
            requestCounter.incrementRejections();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
