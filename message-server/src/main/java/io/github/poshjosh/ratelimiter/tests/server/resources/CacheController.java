package io.github.poshjosh.ratelimiter.tests.server.resources;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.tests.server.MessageServer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/caches")
public class CacheController {
    private final MessageServer.RedisBandwidthStore store;
    public CacheController(MessageServer.RedisBandwidthStore store) {
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
        return getValueFromCache(MessageServer.RedisBandwidthStore.getLastGottenKey());
    }

    @GetMapping("/last/put")
    @Rate("100/s")
    public Object getLastPut() {
        final String lastPutKey = MessageServer.RedisBandwidthStore.getLastPutKey();
        return lastPutKey == null ? null : getValueFromCache(lastPutKey);
    }

    @GetMapping("/last-key/put")
    @Rate("100/s")
    public String getLastPutKey() {
        return MessageServer.RedisBandwidthStore.getLastPutKey();
    }

    private Bandwidth getValueFromCache(String key) {
        return store.get(key);
    }
}
