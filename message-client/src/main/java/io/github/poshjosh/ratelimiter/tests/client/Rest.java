package io.github.poshjosh.ratelimiter.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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
}