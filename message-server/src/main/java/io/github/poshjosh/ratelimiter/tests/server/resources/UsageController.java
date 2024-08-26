package io.github.poshjosh.ratelimiter.tests.server.resources;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.tests.server.RemoteRateLimiter;
import io.github.poshjosh.ratelimiter.tests.server.model.Usage;
import io.github.poshjosh.ratelimiter.tests.server.services.UsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class UsageController{

    private static final Logger log = LoggerFactory.getLogger(UsageController.class);

    public static final String path = "/usage";
    public static final String limited = "/limited";
    private static final String limited_path = path + limited;

    /**
     * Memory below which rate limiting kicks in.
     */
    private static final int memoryThresholdMb = 1700; // max = 1854MB, usage limit 154MB
    private static final String condition = ""; //"web.session.id!=&jvm.memory.available<" + memoryThresholdMb;

    private final UsageService usageService;

    private final RemoteRateLimiter remoteRateLimiter;

    public UsageController(UsageService usageService, RemoteRateLimiter remoteRateLimiter) {
        this.usageService = usageService;
        this.remoteRateLimiter = remoteRateLimiter;
        int [] permits = {500, 100, 50, 25, 15, 10};
        for (int permit : permits) {
            remoteRateLimiter.addRate("usages." + permit, permit+"/s", null);
        }
    }

    @DeleteMapping(path)
    public ResponseEntity<Object> clearUsageRecord() {
        log.debug("#clearUsageRecord()");
        usageService.clearUsageRecord();
        return ResponseEntity.ok().build();
    }

    @GetMapping(path)
    public ResponseEntity<List<Usage>> usage() {
        log.debug("#usage()");
        return ResponseEntity.ok(usageService.usage());
    }

    @GetMapping(limited_path + "/500")
    @Rate(permits = 500, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage500(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.500", request);

        log.debug("#limitedUsage500({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/100")
    @Rate(permits = 100, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage100(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.100", request);

        log.debug("#limitedUsage100({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/50")
    @Rate(permits = 50, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage50(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        log.debug("#limitedUsage50({})", work);
        remoteRateLimiter.checkLimit("usages.50", request);

        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/25")
    @Rate(permits = 25, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage25(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.25", request);

        log.debug("#limitedUsage25({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/15")
    @Rate(permits = 15, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage15(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.15", request);

        log.debug("#limitedUsage10({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/10")
    @Rate(permits = 10, condition = condition)
    public ResponseEntity<BigDecimal> limitedUsage10(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.10", request);

        log.debug("#limitedUsage10({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(limited_path + "/0")
    public ResponseEntity<BigDecimal> limitedUsage0(
            HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int work) {
        remoteRateLimiter.checkLimit("usages.10", request);

        log.debug("#limitedUsage0({})", work);
        return ResponseEntity.ok(usageService.work(work));
    }

    @GetMapping(path + "/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        log.debug("#summary()");
        return ResponseEntity.ok(usageService.summary());
    }
}