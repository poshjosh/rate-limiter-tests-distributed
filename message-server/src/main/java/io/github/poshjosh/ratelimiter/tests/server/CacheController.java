package io.github.poshjosh.ratelimiter.tests.server;

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
    public String getLastGotten() {
        return getStringValueFromCache(MessageServer.RateLimiterConfigurer.getLastGottenKey());
    }

    @GetMapping("/last-put")
    public String getLastPut() {
        return getStringValueFromCache(MessageServer.RateLimiterConfigurer.getLastPutKey());
    }

    private String getStringValueFromCache(String key) {
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }
}
