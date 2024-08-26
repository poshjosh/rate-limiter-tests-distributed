package io.github.poshjosh.ratelimiter.tests.server.redis;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.tests.server.model.Usage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration 
public class RedisConfiguration {

    private final RedisConnectionFactory connectionFactory;

    public RedisConfiguration(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean("redisBandwidthTemplate")
    public RedisTemplate<String, Bandwidth> redisBandwidthTemplate() {
        final RedisTemplate<String, Bandwidth> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean
    public RedisBandwidthStore redisBandwidthStore(
            @Autowired @Qualifier("redisBandwidthTemplate")
            RedisTemplate<String, Bandwidth> redisBandwidthTemplate) {
        return new RedisBandwidthStore(redisBandwidthTemplate);
    }

    @Bean("redisUsageTemplate")
    public RedisTemplate<String, Usage> redisRatesTemplate() {
        final RedisTemplate<String, Usage> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean("redisUsageCache")
    public RedisCache<Usage> redisRatesCache(
            @Autowired @Qualifier("redisUsageTemplate")
            RedisTemplate<String, Usage> redisRatesTemplate) {
        return new RedisCache<>(redisRatesTemplate);
    }


    @Bean("redisLongTemplate")
    public RedisTemplate<String, Long> redisLongTemplate() {
        final RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean("redisLongCache")
    public RedisCache<Long> redisLongCache(
            @Autowired @Qualifier("redisLongTemplate")
            RedisTemplate<String, Long> redisRatesTemplate) {
        return new RedisCache<>(redisRatesTemplate);
    }
}
