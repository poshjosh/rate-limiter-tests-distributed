package io.github.poshjosh.ratelimiter.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTests {

    private static final Logger log = LoggerFactory.getLogger(AbstractTests.class);

    private static int postedMessageCount = 0;
    private static AtomicInteger totalCount = new AtomicInteger();
    private static AtomicInteger failureCount = new AtomicInteger();
    private static final AtomicBoolean inProgress = new AtomicBoolean();
    private final RestTemplate restTemplate = new RestTemplate();

    private final StringBuilder output = new StringBuilder();

    protected AbstractTests() { }

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
        if (isInProgress()) {
            throw new IllegalStateException("In progress");
        }
        try {
            setInProgress(true);
            totalCount.set(0);
            failureCount.set(0);
            String message = doRun();

            return message + "<p>Failures: " + failureCount.get() + " / " + totalCount.get() + "</p>" + output ;
        } finally {
            setInProgress(false);
        }
    }

    protected <T> ResponseEntity<T> sendGetRequest(URI uri, Class<T> responseType, T bodyIfNone) {
        return sendRequest(uri, HttpMethod.GET, Collections.emptyList(), new HttpHeaders(), null, responseType, bodyIfNone);
    }

    protected <T> ResponseEntity<T> sendRequest(
            URI uri, HttpMethod method, Collection<String> cookies,
            HttpHeaders headers, Message requestBody, Class<T> responseType, T bodyIfNone) {
        if (cookies != null && !cookies.isEmpty()) {
            headers.put("Cookie", new ArrayList<>(cookies));
        }
        HttpEntity<Message> entity = new HttpEntity<>(requestBody, headers);
        log.debug("{} {}, with cookies: {}", method, uri, cookies);
        ResponseEntity<T> response;
        try {
            totalCount.incrementAndGet();
            response = restTemplate.exchange(uri, method, entity, responseType);
        } catch(HttpStatusCodeException e) {
            log.warn(e.toString());
            response = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(CharSequence.class.isAssignableFrom(responseType) ? (T)e.getResponseBodyAsString() : bodyIfNone);
        } catch (RestClientException e) {
            failureCount.incrementAndGet();
            throw e;
        }
        T responseBody = response.getBody();
        if (responseBody == null || responseBody.toString().isEmpty()) {
            response = ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(bodyIfNone);
        }
        log(response);
        return response;
    }

    protected <T> boolean shouldReturnStatus(ResponseEntity<T> responseEntity, int expectedStatus) {
        final int status = responseEntity.getStatusCodeValue();
        if (expectedStatus != status) {
            failureCount.incrementAndGet();
            return false;
        } else {
            return true;
        }
    }

    protected Message getRandomRequestBody() {
        ++postedMessageCount;
        Message message = new Message();
        message.setText("random-message" + (postedMessageCount));
        return message;
    }

    protected void log(ResponseEntity responseEntity) {
        log.debug("Response status: {}", responseEntity.getStatusCode());
    }

    protected List<String> getCookies(ResponseEntity responseEntity) {
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        return cookies == null ? Collections.emptyList() : cookies;
    }

    protected AbstractTests appendOutput(Object o) {
        output.append(o);
        return this;
    }
}
