package io.github.poshjosh.ratelimiter.tests.client.performance;

import io.github.poshjosh.ratelimiter.tests.client.AbstractTests;
import io.github.poshjosh.ratelimiter.tests.client.ResourcePaths;
import io.github.poshjosh.ratelimiter.tests.client.performance.process.PerformanceTestStrategy;
import io.github.poshjosh.ratelimiter.tests.client.performance.process.TestProcess;
import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PerformanceTests extends AbstractTests implements TestProcess {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTests.class);


    private final Object mutex = new Object();

    private final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
    private long startTime;
    private final BigDecimal [] lastResultHolder = new BigDecimal[1];

    private final AtomicInteger numberOfThreads = new AtomicInteger();

    private final URI baseUri;

    private final URI uri;

    private final PerformanceTestsResultHandler resultHandler;

    private final PerformanceTestData performanceTestData;

    public PerformanceTests(
            URI baseUri, PerformanceTestData performanceTestData,
            PerformanceTestsResultHandler resultHandler) {
        this.performanceTestData = Objects.requireNonNull(performanceTestData);
        this.baseUri = Objects.requireNonNull(baseUri);
        this.uri = ResourcePaths.performanceTestUri(baseUri, performanceTestData.getLimit(),
                performanceTestData.getTimeout(), performanceTestData.getWork());
        this.resultHandler = resultHandler;
    }

    protected String doRun() {

        final Map statsBefore = fetchStats();

        startTime = System.currentTimeMillis();
        lastResultHolder[0] = null;
        numberOfThreads.set(0);

        final int estResultSizePerIteration = 25_000;
        final int iterations = performanceTestData.getIterations();

        List<Thread> threads = new ArrayList<>(iterations * estResultSizePerIteration);

        List<Usage> resultBuffer = new ArrayList<>(iterations * estResultSizePerIteration);

        for (int i = 0; i < iterations; i++) {
            startTests(String.valueOf(i + 1), threads, resultBuffer);
        }

        waitForAll(threads);

        log.debug("Threads: {}, results: {}", numberOfThreads, resultBuffer.size());

        List<?> usageRateResponse = sendGetRequest(
                baseUri.resolve(ResourcePaths.USAGE_PATH), List.class, Collections.emptyList()).getBody();
        List<Usage> usageRate = usageRateResponse.stream()
                .map(oval -> (Map)oval)
                .map(map -> new Usage(map.get("amount").toString(), map.get("memory").toString()))
                .collect(Collectors.toList());

        return resultHandler.process(statsBefore, resultBuffer, fetchStats(), usageRate);
    }

    private Map fetchStats() {
        return sendGetRequest(
                baseUri.resolve(ResourcePaths.USAGE_SUMMARY_PATH), Map.class, Collections.emptyMap()).getBody();
    }

    private void startTests(String id, List<Thread> threads, List<Usage> resultBuffer) {
        final RequestSpreadType requestSpreadType = performanceTestData.getRequestSpreadType();
        final double percent = (double)performanceTestData.getPercent() / 100;
        if (percent < 0) {
            throw new IllegalArgumentException("Performance test percent < 0: " + percent);
        }
        getTestStrategy(requestSpreadType).run(id, threads, resultBuffer, percent);
    }

    private PerformanceTestStrategy getTestStrategy(RequestSpreadType requestSpreadType) {
        switch (requestSpreadType) {
            case STEEP_GAUSSIAN: return PerformanceTestStrategy.ofSteepGaussian(this);
            case RANDOM_1: return PerformanceTestStrategy.ofRandom1(this);
            case STEADY_1: return PerformanceTestStrategy.ofSteady1(this);
            case GAUSSIAN:
            default: return PerformanceTestStrategy.ofGaussian(this);
        }
    }

    @Override
    public void run(String id, double requestPerSec, List<Thread> threads, List<Usage> resultBuffer) {

        final BigDecimal responseBodyIfNone = MathUtil.ZERO;

        final int intervalMillis = (int)(1000 / requestPerSec);
        final int count = (int)(requestPerSec * performanceTestData.getDurationPerTestUser());

        Set<String> cookies = new HashSet<>();

        for (int i = 0; i < count; i ++) {

            sleep(intervalMillis);

            final String threadName = id + "_" + i;

            Runnable runnable = () -> {
                BigDecimal result = MathUtil.ZERO;
                try {
                    ResponseEntity<Object> responseEntity = sendGetUsageRequest(responseBodyIfNone);
                    cookies.addAll(getCookies(responseEntity));
                    shouldReturnStatus(responseEntity, 200);
                    Object body = responseEntity.getBody();
                    result = MathUtil.toBigDecimal(body);
                }catch(RuntimeException e) {
                    //org.springframework.web.client.ResourceAccessException: I/O error on GET request for "http://nginx:4444/usage/limited": Connection reset; nested exception is java.net.SocketException: Connection reset
                    log.warn("Error in runnable named: {}, error: {}", threadName, e);
                    //throw e;
                }
                synchronized (mutex) {
                    if (MathUtil.ZERO.equals(result)) {
                        if (lastResultHolder[0] != null) {
                            result = lastResultHolder[0];
                        }
                    } else {
                        lastResultHolder[0] = result;
                    }
                }
                BigDecimal timeSecs = MathUtil.divide(BigDecimal.valueOf(System.currentTimeMillis() - startTime), ONE_THOUSAND);
                resultBuffer.add(new Usage(timeSecs, result));
            };

            Thread t = new Thread(runnable, threadName);
            t.start();
            numberOfThreads.incrementAndGet();
            threads.add(t);
        }
    }

    private void waitForAll(List<Thread> threads) {
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                onInterrupted(e);
            }
        });
    }

    private void sleep(long interval) {
        if (interval < 5) {
            return;
        }
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            onInterrupted(e);
        }
    }

    private void onInterrupted(InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    }

    private ResponseEntity<Object> sendGetUsageRequest(Object bodyIfNone) {
        return sendGetRequest(uri, Object.class, bodyIfNone);
    }
}
