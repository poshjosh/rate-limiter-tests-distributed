package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import io.github.poshjosh.ratelimiter.tests.client.RandomHeaders;
import io.github.poshjosh.ratelimiter.tests.client.exception.TestException;
import io.github.poshjosh.ratelimiter.tests.client.services.UsageService;
import io.github.poshjosh.ratelimiter.tests.client.tests.AbstractTests;
import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.PerformanceTestStrategy;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.strategy.TestProcess;
import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PerformanceTests extends AbstractTests implements TestProcess {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTests.class);
    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
    private long startTime;

    private final PerformanceTestsResultHandler resultHandler;

    private final PerformanceTestData performanceTestData;

    private final ExecutorService executorService;

    private final boolean randomizeRequests;

    private final UsageService usageService;

    private int totalDurationSeconds;

    private static Rest withInterceptor(Rest rest, boolean randomizeRequests) {
        if (!randomizeRequests) {
            return rest;
        }
        return rest.withInterceptor((request, body, execution) -> {
            RandomHeaders.randomize(request.getHeaders());
            return execution.execute(request, body);
        });
    }

    public PerformanceTests(
            Rest rest,
            PerformanceTestData performanceTestData,
            PerformanceTestsResultHandler resultHandler,
            boolean randomizeRequests,
            UsageService usageService) {
        super(withInterceptor(rest, randomizeRequests));
        this.performanceTestData = Objects.requireNonNull(performanceTestData);
        this.resultHandler = resultHandler;
        this.executorService = Executors.newCachedThreadPool();
        this.randomizeRequests = randomizeRequests;
        this.usageService = Objects.requireNonNull(usageService);
    }

    protected String doRun() {

        final Map statsBefore = fetchStats();

        startTime = System.currentTimeMillis();

        totalDurationSeconds = 0;

        final int estResultSizePerIteration = 25_000;
        final int iterations = performanceTestData.getIterations();

        List<Usage> resultBuffer = new ArrayList<>(10_000 * iterations * estResultSizePerIteration);

        for (int i = 0; i < iterations; i++) {
            startTests(String.valueOf(i + 1), resultBuffer);
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(totalDurationSeconds, TimeUnit.SECONDS);
        }catch(InterruptedException e) {
            onInterrupted(e);
        } finally{
            log.info("Waited {} seconds for {} results", totalDurationSeconds, resultBuffer.size());
            executorService.shutdownNow();
        }

        List<?> usageRateResponse = usageService.usage().getBody();
        List<Usage> usageRate = usageRateResponse.stream()
                .filter(Map.class::isInstance)
                .map(oval -> (Map)oval)
                .map(map -> new Usage(amount(map), memory(map)))
                .collect(Collectors.toList());

        return resultHandler.process(statsBefore, resultBuffer, fetchStats(), usageRate);
    }

    private BigDecimal amount(Map map) {
        return MathUtil.toBigDecimal(map.get("amount"));
    }

    private BigDecimal memory(Map map) {
        final Object oval = map.get("memory");
        Long lval = oval instanceof Long ? (Long)oval : Long.parseLong(oval.toString());
        return MathUtil.toBigDecimal(String.valueOf(lval / (double)1_000_000));
    }

    private Map fetchStats() {
        return usageService.stats().getBody();
    }

    private void startTests(String id, List<Usage> resultBuffer) {
        PerformanceTestStrategy
                .of(performanceTestData.getRequestSpreadType(), this)
                .run(id, resultBuffer, performanceTestData.getPercent());
    }

    @Override
    public List<CompletableFuture<Usage>> run(String id, double requestPerSec, List<Usage> resultBuffer) {

        final int durationSeconds = performanceTestData.getDurationPerTestUser();

        Set<String> cookies = new HashSet<>();

        final int totalRequests = (int)(durationSeconds * requestPerSec);
        final int interval = (int)((1 / requestPerSec) * 1000);

        List<CompletableFuture<Usage>> futures = new ArrayList<>(totalRequests);

        for (int i = 0; i < totalRequests; i ++) {

            sleep(interval);

            Supplier<Usage> sendUsageRequest =
                    createUsageRequestTask(id + "_" + i, cookies, resultBuffer);

            futures.add(CompletableFuture.supplyAsync(sendUsageRequest, executorService));
        }

        totalDurationSeconds += durationSeconds;

        return Collections.unmodifiableList(futures);
    }

    private Supplier<Usage> createUsageRequestTask(String threadName, Set<String> cookies, List<Usage> resultBuffer) {
        return () -> {
            BigDecimal memory = MathUtil.ZERO;
            try {
                ResponseEntity<Object> responseEntity = sendGetUsageRequest(MathUtil.ZERO);
                if (!this.randomizeRequests) {
                    cookies.addAll(getCookies(responseEntity));
                }
                updateStats(responseEntity, 200);
                Object body = responseEntity.getBody();
                memory = MathUtil.toBigDecimal(body);
            }catch(RuntimeException e) {
                //org.springframework.web.client.ResourceAccessException: I/O error on GET request for "http://nginx:4444/usage/limited": Connection reset; nested exception is java.net.SocketException: Connection reset
                log.warn("Error in runnable named: {}, error: {}", threadName, e);
                //throw e;
            }
            Usage usage = new Usage(timeSinceStart(), memory);
            resultBuffer.add(usage);
            return usage;
        };
    }

    private BigDecimal timeSinceStart() {
        return MathUtil.divide(BigDecimal.valueOf(System.currentTimeMillis() - startTime), ONE_THOUSAND);
    }

    private void sleep(long interval) {
        if (interval < 10) {
            return;
        }
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            onInterrupted(e);
        }
    }

    private void onInterrupted(InterruptedException e) {
        log.error("Interrupted while waiting for tests to complete", e);
        Thread.currentThread().interrupt();
        throw TestException.interrupted();
    }

    private ResponseEntity<Object> sendGetUsageRequest(Object bodyIfNone) {
        return sendGetRequest("", Object.class, bodyIfNone);
    }
}
