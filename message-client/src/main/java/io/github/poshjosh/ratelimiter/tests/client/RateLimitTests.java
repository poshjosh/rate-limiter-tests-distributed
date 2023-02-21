package io.github.poshjosh.ratelimiter.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateLimitTests {

    private static final Logger log = LoggerFactory.getLogger(RateLimitTests.class);
    
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int TOO_MANY = 429;

    private static int postedMessageCount = 0;
    private static int failureCount = 0;
    private static final AtomicBoolean inProgress = new AtomicBoolean();
    private final URI uri;
    private final RestTemplate restTemplate = new RestTemplate();

    private final StringBuilder output = new StringBuilder();

    public RateLimitTests(URI uri) {
        this.uri = Objects.requireNonNull(uri);
    }

    public boolean isInProgress() {
        return inProgress.get();
    }

    /**
     * Run rate limit tests against a remote service
     * @return The number of tests that failed
     * @throws IllegalStateException if an instance of this test is already running
     */
    public String run() {
        if (inProgress.get()) {
            throw new IllegalStateException("In progress");
        }
        try {
            inProgress.compareAndSet(false, true);
            return doRun();
        } finally {
            inProgress.compareAndSet(true, false);
        }
    }

    private String doRun() {

        failureCount = 0;

        final List<String> noCookies = Collections.emptyList();

        // Endpoint GET all is rate limited at 1 per minute when header X-SAMPLE-TRIGGER=true
        // See the associated rest endpoint having the rate limit and condition
        for (int i = 0; i < 5; i++) {
            shouldReturnStatus(givenAllEntitiesAreGotten(noCookies), OK);
        }

        List<String> cookies = getCookies(givenAllEntitiesAreGotten(noCookies));

        // For POST, each session (identified by sessionId) can post only once per minute
        // See the associated rest endpoint having the rate limit and condition
        ResponseEntity<Object> responsePostOne = givenEntityIsPosted(cookies);
        if (cookies == null || cookies.isEmpty()) {
            cookies = getCookies(responsePostOne);
        }
        shouldReturnStatus(responsePostOne, CREATED); // 1 of 1

        shouldReturnStatus(givenEntityIsPosted(cookies), TOO_MANY);  // 2 of 1
        shouldReturnStatus(givenEntityIsPosted(noCookies), CREATED);// No cookies, new session, within limit
        shouldReturnStatus(givenEntityIsPosted(cookies), TOO_MANY);  // 3 of 1
        shouldReturnStatus(givenEntityIsPosted(noCookies), CREATED);// No cookies, new session, within limit

        // For GET, each session (identified by sessionId) can post only once per minute
        // See the associated rest endpoint having the rate limit and condition
        //
        // Different http method, in this case a different endpoint, so within limit
        shouldReturnStatus(givenEntityIsGotten(cookies, 1), OK); // Same session, different endpoint, within limit
        shouldReturnStatus(givenEntityIsGotten(cookies, 2), TOO_MANY); // Same session, repeated endpoint, exceeded limit

        // For DELETE, each call to the endpoint can post only twice per minute
        // Cookies are irrelevant here, as the limit applies to the endpoint, not the session
        // See the associated rest endpoint having the rate limit
        //
        shouldReturnStatus(givenEntityIsDeleted(noCookies, 1), OK);       // 1 of 2
        shouldReturnStatus(givenEntityIsDeleted(noCookies, 2), OK);       // 2 of 2
        shouldReturnStatus(givenEntityIsDeleted(noCookies, 3), TOO_MANY); // 3 of 2

        // Switch to a limit of 1 call per minute by setting the header X-SAMPLE-TRIGGER=true
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-SAMPLE-TRIGGER", "true");
        shouldReturnStatus(givenAllEntitiesAreGotten(noCookies, headers), OK); // 1 of 1
        shouldReturnStatus(givenAllEntitiesAreGotten(noCookies), OK); // No header, no limit
        shouldReturnStatus(givenAllEntitiesAreGotten(noCookies, headers), TOO_MANY); // 2 of 1

        return "Failure count: " + failureCount + output;
    }

    private ResponseEntity<List> givenAllEntitiesAreGotten(List<String> cookies) {
        return givenAllEntitiesAreGotten(cookies, new HttpHeaders());
    }

    private ResponseEntity<List> givenAllEntitiesAreGotten(List<String> cookies, HttpHeaders headers) {
        return sendRequest(uri, HttpMethod.GET, cookies, headers, null, List.class, Collections.emptyList());
    }

    private ResponseEntity<Object> givenEntityIsGotten(List<String> cookies, long id) {
        return sendRequest(HttpMethod.GET, cookies, id);
    }

    private ResponseEntity<Object> givenEntityIsPosted(List<String> cookies) {
        return sendRequest(HttpMethod.POST, cookies, getRandomRequestBody());
    }

    private ResponseEntity<Object> givenEntityIsDeleted(List<String> cookies, long id) {
        URI uri = URI.create(this.uri + "/" + id);
        return sendRequest(uri, HttpMethod.DELETE, cookies, new HttpHeaders(), null, Object.class, "");
    }

    private ResponseEntity<Object> sendRequest(HttpMethod method, List<String> cookies, long id) {
        URI uri = URI.create(this.uri + "/" + id);
        return sendRequest(uri, method, cookies, new HttpHeaders(), null, Object.class, "");
    }

    private ResponseEntity<Object> sendRequest(HttpMethod method, List<String> cookies, Message body) {
        return sendRequest(uri, method, cookies, new HttpHeaders(), body, Object.class, "");
    }

    private <T> ResponseEntity<T> sendRequest(
            URI uri, HttpMethod method, List<String> cookies,
            HttpHeaders headers, Message body, Class<T> responseType, T bodyIfNone) {
        if (cookies != null && !cookies.isEmpty()) {
            headers.put("Cookie", cookies);
        }
        HttpEntity<Message> entity = new HttpEntity<>(body, headers);
        log.info("{} {}, with cookies: {}", method, uri, cookies);
        ResponseEntity<T> response;
        try {
            response = restTemplate.exchange(uri, method, entity, responseType);
        } catch(HttpStatusCodeException e) {
            log.warn(e.toString());
            response = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(bodyIfNone);
        }
        log(response);
        return response;
    }

    private <T> boolean shouldReturnStatus(ResponseEntity<T> responseEntity, int expectedStatus) {
        final int status = responseEntity.getStatusCodeValue();
        final boolean result;
        if (expectedStatus != status) {
            ++failureCount;
            result = false;
        } else {
            result = true;
        }
        final String resultStr = result ? "SUCCESS" : "FAILURE";
        log.info(resultStr);
        final T body = responseEntity.getBody();
        output.append("<br/><br/>").append(resultStr).append(" ").append(status)
                .append(" Trace:<br/>").append(body);
        return result;
    }

    private Message getRandomRequestBody() {
        ++postedMessageCount;
        Message message = new Message();
        message.setText("random-message" + (postedMessageCount));
        return message;
    }

    private void log(ResponseEntity responseEntity) {
        log.info("Response status: {}", responseEntity.getStatusCode());
    }

    private List<String> getCookies(ResponseEntity responseEntity) {
        return responseEntity.getHeaders().get("Set-Cookie");
    }
}
