package io.github.poshjosh.ratelimiter.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
public class Rest {
    private final Logger log = LoggerFactory.getLogger(Rest.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String remoteServerUrl;

    public Rest(@Value("${app.message-server.url}") String remoteServerUrl) {
        this.remoteServerUrl = remoteServerUrl;
    }

    public Rest withPath(String path) {
        return new Rest(remoteServerUrl + path);
    }

    public URI createServerUri(String path) {
        return URI.create(remoteServerUrl + path);
    }

    public <T> ResponseEntity<T> getFromServer(String path, Class<T> resultType,
            Function<RestClientException, T> onError) {
        return get(createServerUri(path), resultType, onError);
    }

    private <T> ResponseEntity<T> get(URI uri, Class<T> resultType,
            Function<RestClientException, T> onError) {
        try {
            return restTemplate.getForEntity(uri, resultType);
        } catch(RestClientException e) {
            log.warn("Error accessing: " + uri, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(onError.apply(e));
        }
    }

    protected <T> ResponseEntity<T> sendGetRequest(String path, Class<T> responseType, T bodyIfNone) {
        return sendRequest(path, HttpMethod.GET, Collections.emptyList(), new HttpHeaders(), null, responseType, bodyIfNone);
    }

    protected <T> ResponseEntity<T> sendRequest(
            String path, HttpMethod method, Collection<String> cookies,
            HttpHeaders headers, Message requestBody, Class<T> responseType, T bodyIfNone) {
        if (cookies != null && !cookies.isEmpty()) {
            headers.put("Cookie", new ArrayList<>(cookies));
        }
        HttpEntity<Message> entity = new HttpEntity<>(requestBody, headers);
        final URI uri = createServerUri(path);
        log.debug("{} {}, with cookies: {}", method, uri, cookies);
        ResponseEntity<T> response;
        try {
            response = restTemplate.exchange(uri, method, entity, responseType);
        } catch(HttpStatusCodeException e) {
            log.warn(e.toString());
            response = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(CharSequence.class.isAssignableFrom(responseType) ? (T)e.getResponseBodyAsString() : bodyIfNone);
        } catch (RestClientException e) {
            throw e;
        }
        T responseBody = response.getBody();
        if (responseBody == null || responseBody.toString().isEmpty()) {
            response = ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(bodyIfNone);
        }
        return response;
    }
}