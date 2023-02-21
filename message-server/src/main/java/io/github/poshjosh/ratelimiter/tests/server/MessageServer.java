package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.UsageListener;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import io.github.poshjosh.ratelimiter.util.LimiterConfig;
import io.github.poshjosh.ratelimiter.web.core.Registries;
import io.github.poshjosh.ratelimiter.web.core.ResourceLimiterConfigurer;
import io.github.poshjosh.ratelimiter.web.core.util.RateLimitProperties;
import io.github.poshjosh.ratelimiter.web.spring.RateLimitPropertiesSpring;
import io.github.poshjosh.ratelimiter.web.spring.ResourceLimitingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties(MessageServer.RateLimitPropertiesImpl.class)
public class MessageServer {
    private static final Logger log = LoggerFactory.getLogger(MessageServer.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MessageServer.class, args);
        Trace.init();
        Startup.log(ctx.getEnvironment());
    }

    @Bean
    public InitialMessagesPersister initialMessagesPersister(MessageService messageService) {
        return new InitialMessagesPersister(messageService);
    }

    @ConfigurationProperties(prefix = "rate-limiter", ignoreUnknownFields = false)
    public static class RateLimitPropertiesImpl extends RateLimitPropertiesSpring { }

    @Component
    public static class RateLimitingFilter extends ResourceLimitingFilter {
        public RateLimitingFilter(
                RateLimitProperties properties,
                ResourceLimiterConfigurer configurer) {
            super(properties, configurer);
        }
        @Override
        protected void onLimitExceeded(
                HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws IOException {
            log.warn("Too many requests");
            res.sendError(429, Trace.getAndClear().toString());
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
    public static class RateLimiterConfigurer implements ResourceLimiterConfigurer,
            BandwidthsStore<String>, UsageListener {
        private static ThreadLocal<String> lastGottenKey = new ThreadLocal<>();
        private static ThreadLocal<String> lastPutKey = new ThreadLocal<>();
        private final RedisTemplate<String, Bandwidth> redisTemplate;
        public RateLimiterConfigurer(RedisTemplate<String, Bandwidth> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
        public static String getLastGottenKey() {
            return lastGottenKey.get();
        }
        public static String getLastPutKey() {
            return lastPutKey.get();
        }
        @Override
        public void configure(Registries registries) {
            log.info("Configuring rate limiter");
            registries.registerStore(this);
            registries.registerListener(this);
        }
        @Override public Bandwidth get(String key) {
            Bandwidth bandwidth = redisTemplate.opsForValue().get(key);
            lastGottenKey.set(key);
            log.info("#get {}={}", key, bandwidth);
            return bandwidth;
        }
        @Override public void put(String key, Bandwidth bandwidth) {
            redisTemplate.opsForValue().set(key, bandwidth);
            lastPutKey.set(key);
            log.info("#put {}={}", key, bandwidth);
        }
        @Override public void onConsumed(Object req, String id, int permits, LimiterConfig<?> cfg) {
            log.info("#onConsumed {} {}", id, cfg.getRates());
        }
        @Override public void onRejected(Object req, String id, int permits, LimiterConfig<?> cfg) {
            log.info("#onRejected {} {}", id, cfg.getRates());
        }
    }

}