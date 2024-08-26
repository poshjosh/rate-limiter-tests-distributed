package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.RateLimiter;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import io.github.poshjosh.ratelimiter.tests.server.model.RateLimitMode;
import io.github.poshjosh.ratelimiter.tests.server.services.MessageService;
import io.github.poshjosh.ratelimiter.tests.server.util.logging.LogMessages;
import io.github.poshjosh.ratelimiter.tests.server.util.logging.LogMessageCollector;
import io.github.poshjosh.ratelimiter.tests.server.util.EnvLogger;
import io.github.poshjosh.ratelimiter.util.RateLimitProperties;
import io.github.poshjosh.ratelimiter.web.core.WebRateLimiterContext;
import io.github.poshjosh.ratelimiter.web.core.WebRateLimiterRegistry;
import io.github.poshjosh.ratelimiter.web.spring.RateLimitPropertiesSpring;
import io.github.poshjosh.ratelimiter.web.spring.RateLimitingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(MessageServerApplication.RateLimitPropertiesImpl.class)
public class MessageServerApplication {
    private static final Logger log = LoggerFactory.getLogger(MessageServerApplication.class);

    public static void main(String[] args) {
        EnvLogger.log(SpringApplication.run(MessageServerApplication.class, args).getEnvironment());
        LogMessageCollector.init(); // Must be initialized after spring application startup
    }

    @Bean
    public InitialMessagesPersister initialMessagesPersister(MessageService messageService) {
        return new InitialMessagesPersister(messageService);
    }

    @ConfigurationProperties(prefix = "rate-limiter", ignoreUnknownFields = false)
    public static class RateLimitPropertiesImpl extends RateLimitPropertiesSpring { }

    @Component
    public static class RateLimitingFilterImpl extends RateLimitingFilter {
        private final BandwidthsStore<String> store;
        private final ManualRateLimiter manualRateLimiter;
        private final RateLimitMode rateLimitMode;
        private final RequestCounter requestCounter;
        public RateLimitingFilterImpl(
                RateLimitProperties properties, BandwidthsStore<String> store,
                ManualRateLimiter manualRateLimiter, @Value("${app.rate-limit-mode}") String rateLimitModeString,
                RequestCounter requestCounter) {
            super(properties);
            this.store = Objects.requireNonNull(store);
            this.manualRateLimiter = Objects.requireNonNull(manualRateLimiter);
            this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
            this.requestCounter = Objects.requireNonNull(requestCounter);
        }

        @Override protected void initFilterBean() throws ServletException {
            if (RateLimitMode.Auto.equals(rateLimitMode)) {
                super.initFilterBean(); // Initialize rate limiting
            }
        }
        @Override
        protected WebRateLimiterContext.Builder rateLimiterContextBuilder() {
            log.debug("Adding store: {}", store);
            return super.rateLimiterContextBuilder().store(store);
        }

        @Override
        protected WebRateLimiterRegistry rateLimiterRegistry(WebRateLimiterContext context) {
            log.debug("Rate limiter context\n{}", context);
            return super.rateLimiterRegistry(context);
        }

        @Override public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            requestCounter.incrementRequests();
            switch(rateLimitMode) {
                case Auto: super.doFilter(request, response, chain); break; // Apply rate limiting
                case Manual:
                    tryConsume(manualRateLimiter,
                            (HttpServletRequest)request, (HttpServletResponse) response, chain);
                    break;
                case Off:
                case Remote: // Remote rate limiting is handled in the controllers
                default: chain.doFilter(request, response); break;
            }
        }
        private void tryConsume(
                ManualRateLimiter manualRateLimiter, HttpServletRequest request,
                HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            final boolean success = manualRateLimiter.tryConsume(request);
            RequestData requestData = new RequestData(request);
            if (!success) {
                this.onRejected(requestData, manualRateLimiter);
                this.onLimitExceeded(request, response, chain);
                return;
            }
            chain.doFilter(request, response);
        }
        @Override
        protected boolean tryConsume(HttpServletRequest request) {
            final RateLimiter rateLimiter = getRateLimiter(request);
            final boolean success = rateLimiter.tryAcquire(getTimeout(request), TimeUnit.SECONDS);
            final RequestData requestData = new RequestData(request);
            if (!success) {
                onRejected(requestData, rateLimiter);
            }
            return success;
        }
        private int getTimeout(ServletRequest request) {
            final String timeoutStr = request.getParameter("timeout");
            return StringUtils.hasText(timeoutStr) ? Integer.parseInt(timeoutStr) : 0;
        }
        @Override
        protected void onLimitExceeded(
                HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws IOException {
            //log.debug("Too many requests"); Already logged below
            res.sendError(429, LogMessages.getAndClear().toString());
        }
        void onRejected(RequestData requestData, Object rateLimiter) {
            requestCounter.incrementRejections();
            log.debug("Rejected {}, by rate limiter: {}", requestData, rateLimiter);
        }
    }
}