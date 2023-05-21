package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/caches")
public class CacheController {
    private final RedisTemplate<String, Bandwidth> redisTemplate;

    public CacheController(RedisTemplate<String, Bandwidth> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public String home() {
        return "Cache Service";
    }

    @GetMapping("/{key}")
    public String get(@PathVariable("key") String key) {
        return getStringValueFromCache(key);
    }

    @GetMapping("/last-gotten")
    @Rate(100)
    public String getLastGotten() {
        return getStringValueFromCache(MessageServer.RedisBandwidthStore.getLastGottenKey());
    }

    @GetMapping("/last-put")
    @Rate(100)
    public String getLastPut() {
        return getStringValueFromCache(MessageServer.RedisBandwidthStore.getLastPutKey());
    }

    private String getStringValueFromCache(String key) {
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }
}
