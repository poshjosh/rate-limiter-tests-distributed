package io.github.poshjosh.ratelimiter.tests.server.resources;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.tests.server.MessageServerApplication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/caches")
public class CacheController {
    private final MessageServerApplication.RedisBandwidthStore store;
    public CacheController(MessageServerApplication.RedisBandwidthStore store) {
        this.store = store;
    }

    @GetMapping
    public String home() {
        return "Cache Service";
    }

    @GetMapping("/{key}")
    public Object get(@PathVariable("key") String key) {
        return getValueFromCache(key);
    }

    @GetMapping("/last/gotten")
    @Rate("100/s")
    public Object getLastGotten() {
        return getValueFromCache(MessageServerApplication.RedisBandwidthStore.getLastGottenKey());
    }

    @GetMapping("/last/put")
    @Rate("100/s")
    public Object getLastPut() {
        final String lastPutKey = MessageServerApplication.RedisBandwidthStore.getLastPutKey();
        return lastPutKey == null ? null : getValueFromCache(lastPutKey);
    }

    @GetMapping("/last-key/put")
    @Rate("100/s")
    public String getLastPutKey() {
        return MessageServerApplication.RedisBandwidthStore.getLastPutKey();
    }

    private Bandwidth getValueFromCache(String key) {
        return store.get(key);
    }
}
