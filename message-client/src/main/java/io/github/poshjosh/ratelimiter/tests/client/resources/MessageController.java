package io.github.poshjosh.ratelimiter.tests.client.resources;

import io.github.poshjosh.ratelimiter.tests.client.model.Message;
import io.github.poshjosh.ratelimiter.tests.client.Rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@RestController
public class MessageController {

    private final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final Rest rest;
    public MessageController(Rest rest) {
        this.rest = rest;
    }

    @GetMapping(ResourcePaths.MESSAGE_PATH)
    public ResponseEntity<List> getMessages() {
        log.debug("#getMessages()");
        Function<RestClientException, List> onError = e -> Collections.singletonList(e.toString());
        return rest.get(ResourcePaths.MESSAGE_PATH, List.class, onError);
    }

    @GetMapping(ResourcePaths.MESSAGE_PATH + "/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable("id") Long id) {
        log.debug("#getMessage({})", id);
        Function<RestClientException, Message> onError = t -> {
            Message message = new Message();
            message.setId(1000L); // Evil must be resisted
            message.setText(t.getLocalizedMessage());
            return message;
        };
        return rest.get(ResourcePaths.MESSAGE_PATH + "/" + id, Message.class, onError);
    }
}