package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.RateLimiter;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import io.github.poshjosh.ratelimiter.util.RateLimitProperties;
import io.github.poshjosh.ratelimiter.web.core.WebRateLimiterContext;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
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
import java.util.concurrent.atomic.AtomicLong;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(MessageServer.RateLimitPropertiesImpl.class)
public class MessageServer {
    private static final Logger log = LoggerFactory.getLogger(MessageServer.class);

    public static void main(String[] args) {
        Startup.log(SpringApplication.run(MessageServer.class, args).getEnvironment());
        Trace.init(); // Must be initialized after spring application startup
    }

    @Bean
    public InitialMessagesPersister initialMessagesPersister(MessageService messageService) {
        return new InitialMessagesPersister(messageService);
    }

    @ConfigurationProperties(prefix = "rate-limiter", ignoreUnknownFields = false)
    public static class RateLimitPropertiesImpl extends RateLimitPropertiesSpring { }

    @Component
    public static class RateLimitingFilterImpl extends RateLimitingFilter {
        private final AtomicLong requests = new AtomicLong();
        private final AtomicLong consumption = new AtomicLong();
        private final AtomicLong rejection = new AtomicLong();
        private final BandwidthsStore<String> store;
        private final ManualRateLimiter manualRateLimiter;
        private final RateLimitMode rateLimitMode;
        public RateLimitingFilterImpl(
                RateLimitProperties properties, BandwidthsStore<String> store,
                ManualRateLimiter manualRateLimiter, @Value("${app.rate-limit-mode}") String rateLimitModeString) {
            super(properties);
            this.store = Objects.requireNonNull(store);
            this.manualRateLimiter = Objects.requireNonNull(manualRateLimiter);
            this.rateLimitMode = RateLimitMode.of(rateLimitModeString);
        }
        @Override protected void initFilterBean() throws ServletException {
            if (RateLimitMode.Auto.equals(rateLimitMode)) {
                super.initFilterBean(); // Initialize rate limiting
            }
        }
        @Override public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            requests.incrementAndGet();
            switch(rateLimitMode) {
                case Auto: super.doFilter(request, response, chain); break; // Apply rate limiting
                case Manual: applyManualRateLimit(request, response, chain); break;
                case Off:
                default: chain.doFilter(request, response); break;
            }
        }
        private void applyManualRateLimit(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            final boolean success = manualRateLimiter.tryConsume(request);
            RequestData requestData = null;
            if (request instanceof HttpServletRequest) {
                requestData = new RequestData((HttpServletRequest)request);
            }
            this.onConsumed(requestData, manualRateLimiter);
            if (!success) {
                this.onRejected(requestData, manualRateLimiter);
                this.onLimitExceeded((HttpServletRequest)request, (HttpServletResponse)response, chain);
                return;
            }
            chain.doFilter(request, response);
        }
        @Override
        protected boolean tryConsume(HttpServletRequest request) {
            final RateLimiter rateLimiter = getRateLimiterFactory().getRateLimiter(request);
            final boolean success = rateLimiter.tryAcquire(getTimeout(request), TimeUnit.SECONDS);
            final RequestData requestData = new RequestData(request);
            onConsumed(requestData, rateLimiter);
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
            log.debug("Too many requests");
            res.sendError(429, Trace.getAndClear().toString());
        }

        @Override
        protected WebRateLimiterContext.Builder rateLimiterContextBuilder() {
            return super.rateLimiterContextBuilder().store(store);
        }
        public void onConsumed(RequestData requestData, Object rateLimiter) {
            consumption.incrementAndGet();
            log.debug("Consumed {}", rateLimiter);
        }
        public void onRejected(RequestData requestData, Object rateLimiter) {
            rejection.incrementAndGet();
            log.debug("Rejected {}", rateLimiter);
        }
        public long getRequests() {
            return requests.get();
        }
        public long getConsumption() {
            return consumption.get();
        }
        public long getRejection() {
            return rejection.get();
        }
    }

    @Configuration
    public static class RedisConfiguration {
        private final RedisTemplate<String, Bandwidth> redisTemplate;
        public RedisConfiguration(RedisConnectionFactory connectionFactory) {
            this.redisTemplate = new RedisTemplate<>();
            this.redisTemplate.setConnectionFactory(connectionFactory);
            this.redisTemplate.setKeySerializer(RedisSerializer.string());
            this.redisTemplate.setHashKeySerializer(RedisSerializer.string());
        }
        @Bean
        public RedisTemplate<String, Bandwidth> redisTemplate() {
            return redisTemplate;
        }
    }

    @Component
    public static class RedisBandwidthStore implements BandwidthsStore<String> {
        private static final ThreadLocal<String> lastGottenKey = new ThreadLocal<>();
        private static final ThreadLocal<String> lastPutKey = new ThreadLocal<>();
        private final RedisTemplate<String, Bandwidth> redisTemplate;
        public RedisBandwidthStore(RedisTemplate<String, Bandwidth> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
        public static String getLastGottenKey() {
            return lastGottenKey.get();
        }
        public static String getLastPutKey() {
            return lastPutKey.get();
        }
        @Override public Bandwidth get(String key) {
            key = formatCacheKey(key);
            Bandwidth bandwidth = redisTemplate.opsForValue().get(key);
            lastGottenKey.set(key);
            log.debug("#get {}={}", key, bandwidth);
            return bandwidth;
        }
        @Override public void put(String key, Bandwidth bandwidth) {
            key = formatCacheKey(key);
            redisTemplate.opsForValue().set(key, bandwidth);
            lastPutKey.set(key);
            log.debug("#put {}={}", key, bandwidth);
        }

        private String formatCacheKey(String key) {
            // On examining the cache, we found the following prefix on sessionId's
            // These prefixes, if left, may mangle our sessionId-relying logic.
            key = removePrefix(key, "spring:session:sessions:expires:");
            key = removePrefix(key, "spring:session:sessions:");
            return removePrefix(key, "spring:session:expirations:");
        }

        private String removePrefix(String text, String prefixToRemove) {
            if (text.startsWith(prefixToRemove)) {
                text = text.substring(prefixToRemove.length());
            }
            return text;
        }
    }

}