package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
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

    public URI createEndpoint(String path) {
        return URI.create(remoteServerUrl + path);
    }

    public <T> ResponseEntity<T> get(String path, Class<T> resultType,
            Function<RestClientException, T> onError) {
        return doGet(path, resultType, onError);
    }

    private <T> ResponseEntity<T> doGet(String path, Class<T> resultType,
            Function<RestClientException, T> onError) {
        try {
            return sendRequest(path, HttpMethod.GET, null, new HttpHeaders(), null, resultType, null);
        } catch(RestClientException e) {
            log.warn("Error accessing: " + path, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(onError.apply(e));
        }
    }

    public <T> ResponseEntity<T> sendRequest(
            String path, HttpMethod method, Collection<String> cookies,
            HttpHeaders headers, Message requestBody, Class<T> responseType, T bodyIfNone) {
        if (cookies != null && !cookies.isEmpty()) {
            headers.put("Cookie", new ArrayList<>(cookies));
        }
        HttpEntity<Message> entity = new HttpEntity<>(requestBody, headers);
        final URI uri = createEndpoint(path);
        log.debug("{} {}, with cookies: {}", method, uri, cookies);
        ResponseEntity<T> response;
        try {
            response = restTemplate.exchange(uri, method, entity, responseType);
        } catch(HttpStatusCodeException e) {
            log.warn(e.toString());
            response = ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(CharSequence.class.isAssignableFrom(responseType) ? (T)e.getResponseBodyAsString() : bodyIfNone);
        }
        T responseBody = response.getBody();
        if (responseBody == null || responseBody.toString().isEmpty()) {
            response = ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(bodyIfNone);
        }
        return response;
    }

    public Rest withInterceptor(ClientHttpRequestInterceptor interceptor) {
        Rest rest = new Rest(remoteServerUrl);
        rest.restTemplate.getInterceptors().add(interceptor);
        return rest;
    }
}