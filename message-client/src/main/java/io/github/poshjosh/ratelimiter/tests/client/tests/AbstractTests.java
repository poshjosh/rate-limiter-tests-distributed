package io.github.poshjosh.ratelimiter.tests.client.tests;

import io.github.poshjosh.ratelimiter.tests.client.model.Message;
import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.exception.TestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public abstract class AbstractTests {

    private static final Logger log = LoggerFactory.getLogger(AbstractTests.class);

    private static int postedMessageCount = 0;

    // TODO - These counts are not properly implemented
    private static final AtomicInteger totalCount = new AtomicInteger();
    private static final AtomicInteger failureCount = new AtomicInteger();
    private static final AtomicInteger rateLimitCount = new AtomicInteger();
    private static final AtomicBoolean inProgress = new AtomicBoolean();
    private final Rest rest;

    private final StringBuilder output = new StringBuilder();

    protected AbstractTests(Rest rest) {
        this.rest = Objects.requireNonNull(rest);
    }

    protected abstract String doRun();

    public boolean isInProgress() {
        return inProgress.get();
    }
    private void setInProgress(boolean flag) {
        if (flag) {
            inProgress.compareAndSet(false, true);
            return;
        }
        inProgress.compareAndSet(true, false);
    }

    /**
     * Run rate limit tests against a remote service
     * @return The number of tests that failed
     * @throws IllegalStateException if an instance of this test is already running
     */
    public String run() {
        log.info("#run()");
        if (isInProgress()) {
            throw TestException.alreadyRunning();
        }
        try {
            setInProgress(true);
            totalCount.set(0);
            failureCount.set(0);
            rateLimitCount.set(0);
            String message = doRun();

            return message + "<p>Failures: " + failureCount.get() + " / " + totalCount.get()
                    + "</p>" + output + "<p>Failures excluding rate limit rejections: "
                    + (failureCount.get() - rateLimitCount.get()) + " / " + totalCount.get() + "</p>";
        } finally {
            setInProgress(false);
        }
    }

    protected <T> ResponseEntity<T> sendGetRequest(String path, Class<T> responseType, T bodyIfNone) {
        return sendRequest(path, HttpMethod.GET, Collections.emptyList(), new HttpHeaders(), null, responseType, bodyIfNone);
    }

    protected <T> ResponseEntity<T> sendRequest(
            String path, HttpMethod method, Collection<String> cookies,
            HttpHeaders headers, Message requestBody, Class<T> responseType, T bodyIfNone) {
        try {
            totalCount.incrementAndGet();
            return rest.sendRequest(
                    path, method, cookies, headers, requestBody, responseType, bodyIfNone);
        } catch (RestClientException e) {
            if (e instanceof RestClientResponseException) {
                final RestClientResponseException re = (RestClientResponseException)e;
                final int status = re.getRawStatusCode();
                if (status == 429) {
                    rateLimitCount.incrementAndGet();
                }
                return ResponseEntity.status(status)
                        .headers(re.getResponseHeaders())
                        .body(CharSequence.class.isAssignableFrom(responseType) ? (T)re.getResponseBodyAsString() : bodyIfNone);
            }
            failureCount.incrementAndGet();
            log.warn("Error executing: {} {}, {}", method, path, e.toString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    protected <T> boolean shouldReturnStatus(ResponseEntity<T> responseEntity, int... expectedStatuses) {
        return updateStats(responseEntity, expectedStatuses);
    }

    protected <T> boolean updateStats(ResponseEntity<T> responseEntity, int... expectedStatuses) {
        final int status = responseEntity.getStatusCodeValue();
        if (IntStream.of(expectedStatuses).anyMatch(expected -> expected == status)) {
            return true;
        } else {
            if (status == 429) {
                rateLimitCount.incrementAndGet();
            }
            failureCount.incrementAndGet();
            return false;
        }
    }

    protected Message getRandomRequestBody() {
        ++postedMessageCount;
        Message message = new Message();
        message.setText("random-message" + (postedMessageCount));
        return message;
    }

    protected Message getMessageZero() {
        return getMessageZero("Server response has no body");
    }

    protected Message getMessageZero(String text) {
        Message message = new Message();
        message.setId(0L);
        message.setText(text);
        return message;
    }

    protected List<String> getCookies(ResponseEntity responseEntity) {
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        return cookies == null ? Collections.emptyList() : cookies;
    }

    protected AbstractTests appendOutput(Object o) {
        output.append(o);
        return this;
    }

    public Rest getRest() {
        return rest;
    }
}
