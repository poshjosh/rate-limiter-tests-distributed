package io.github.poshjosh.ratelimiter.tests.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@RestController
public class MessageController implements ErrorController {

    private final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final String path = "/messages";

    private final RestTemplate restTemplate = new RestTemplate();

    private final String remoteServerUrl;

    public MessageController(@Value("${app.message-server.url}") String remoteServerUrl) {
        this.remoteServerUrl = remoteServerUrl;
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request) {
        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final String sval = oval == null ? "" : oval.toString();
        return sval.isEmpty() ? "An unexpected error occurred" : sval;
    }

    @GetMapping
    public String home() {
        return "Message Client<p><a href='" + path + "/tests'>Click here to run tests</a></p>";
    }

    @GetMapping(path + "/tests")
    public String tests() {
        return new RateLimitTests(URI.create(remoteServerUrl + path)).run();
    }

    @GetMapping(path)
    public ResponseEntity<List> getMessages() {
        log.info("#getMessages()");
        URI uri = createServerUri(path);
        Function<RestClientException, List> onError = e -> Collections.singletonList(e.toString());
        return get(uri, List.class, onError);
    }

    @GetMapping(path + "/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable("id") Long id) {
        log.info("#getMessage({})", id);
        URI uri = createServerUri(path + "/" + id);
        Function<RestClientException, Message> onError = t -> {
            Message message = new Message();
            message.setId(1000L); // Evil must be resisted
            message.setText(t.getLocalizedMessage());
            return message;
        };
        return get(uri, Message.class, onError);
    }

    private URI createServerUri(String path) {
        return URI.create(remoteServerUrl + path);
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