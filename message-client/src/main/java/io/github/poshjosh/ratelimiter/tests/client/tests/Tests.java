package io.github.poshjosh.ratelimiter.tests.client.tests;

import io.github.poshjosh.ratelimiter.tests.client.Rest;
import io.github.poshjosh.ratelimiter.tests.client.exception.TestException;
import io.github.poshjosh.ratelimiter.tests.client.resources.ResourcePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tests extends AbstractTests {
    private static final Logger log = LoggerFactory.getLogger(Tests.class);

    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int TOO_MANY = 429;

    private final Rest serverRestService;
    public Tests(Rest serverRestService) {
        super(serverRestService.withPath(ResourcePaths.MESSAGE_PATH));
        this.serverRestService = serverRestService;
    }

    protected String doRun() {

        final List<String> noCookies = Collections.emptyList();

        // Endpoint GET all is rate limited at 1 per minute when header X-SAMPLE-TRIGGER=true
        // See the associated rest endpoint having the rate limit and condition
        for (int i = 0; i < 5; i++) {
            shouldReturnStatus(givenAllEntitiesAreGotten(noCookies), OK);

            ResponseEntity<Object> response = getLastPutToCache();
            Object lastPutToCache = response == null ? null : response.getBody();
            if (lastPutToCache == null || lastPutToCache instanceof Throwable) {
                throw TestException.cacheProblem();
            }
        }

        ResponseEntity<List> responseList = givenAllEntitiesAreGotten(noCookies);
        List<String> cookies = getCookies(responseList);
        shouldReturnStatus(responseList, OK);

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

        return "";
    }

    protected ResponseEntity<Object> getLastPutToCache() {
        return serverRestService.get("/caches/last/put", Object.class, e -> e.toString());
    }

    protected ResponseEntity<Object> getLastGottenFromCache() {
        return serverRestService.get("/caches/last/gotten", Object.class, e -> e.toString());
    }

    private ResponseEntity<List> givenAllEntitiesAreGotten(List<String> cookies) {
        return givenAllEntitiesAreGotten(cookies, new HttpHeaders());
    }

    private ResponseEntity<List> givenAllEntitiesAreGotten(List<String> cookies, HttpHeaders headers) {
        return sendRequest("", HttpMethod.GET, cookies, headers, null, List.class,
                Collections.singletonList(getMessageZero()));
    }

    private ResponseEntity<Object> givenEntityIsGotten(List<String> cookies, long id) {
        return sendLongRequest(HttpMethod.GET, cookies, id);
    }

    private ResponseEntity<Object> givenEntityIsPosted(List<String> cookies) {
        return sendRequest("", HttpMethod.POST, cookies, new HttpHeaders(),
                getRandomRequestBody(), Object.class, getRandomRequestBody());
    }

    private ResponseEntity<Object> givenEntityIsDeleted(List<String> cookies, long id) {
        return sendRequest("/" + id, HttpMethod.DELETE, cookies, new HttpHeaders(), null,
                Object.class, false);
    }

    private ResponseEntity<Object> sendLongRequest(HttpMethod method, List<String> cookies, long id) {
        return sendRequest("/" + id, method, cookies, new HttpHeaders(), null, Object.class,
                "Server response has no content");
    }

    protected <T> boolean shouldReturnStatus(ResponseEntity<T> responseEntity, int... expectedStatuses) {
        final int status = responseEntity.getStatusCodeValue();
        final boolean success = super.shouldReturnStatus(responseEntity, expectedStatuses);
        final String resultStr = success ? "<br><br/>SUCCESS, status = " + status :
                "<br/><br/>FAILURE, status = " + status + ", expected any of: " + Arrays.toString(expectedStatuses);
        log.debug(resultStr);
        final T body = responseEntity.getBody();
        appendOutput(resultStr)
                .appendOutput(" Request body:<br/>").appendOutput(body);
        return success;
    }
}
