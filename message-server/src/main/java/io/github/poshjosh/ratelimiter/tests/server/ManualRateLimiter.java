package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.RateLimiter;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Component
public class ManualRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(ManualRateLimiter.class);
    private RateLimiter rateLimiter;
    public boolean tryConsume(ServletRequest request) {
        final String uri = ((HttpServletRequest)request).getRequestURI();
        // Our rate limited endpoints are of format /limited/[DIGITS]
        // The digits are at the end
        // Since we dynamically initialize our manual rate limiter,
        // we use the first rate-limited endpoint.
        // This setup only works for performance tests
        // in which case the same endpoint is called repeatedly
        if (!uri.contains(UsageController.limited)) {
            return true;
        }
        if (rateLimiter == null) {
            final int permitsPerSec = getRateSeconds(uri);
            rateLimiter = permitsPerSec > 0 ? RateLimiter.of(Bandwidth.allOrNothing(permitsPerSec)) : RateLimiter.NO_LIMIT;
            log.info("Completed setup of manual rate limiting at {} permits per second", permitsPerSec);
        }
        final int timeout = getTimeout(request);
        return rateLimiter.tryAcquire(1, timeout, TimeUnit.SECONDS);
    }
    private int getRateSeconds(String uri) {
        final int offset = uri.lastIndexOf('/');
        return Integer.parseInt(uri.substring(offset + 1));
    }
    private int getTimeout(ServletRequest request) {
        final String timeoutStr = request.getParameter("timeout");
        return StringUtils.hasText(timeoutStr) ? Integer.parseInt(timeoutStr) : 0;
    }
}
