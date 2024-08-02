package io.github.poshjosh.ratelimiter.tests.server.resources;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.tests.server.model.Message;
import io.github.poshjosh.ratelimiter.tests.server.services.MessageService;
import io.github.poshjosh.ratelimiter.tests.server.util.logging.LogMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private static final String path = "/messages";

    private final MessageService messageService;
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id !=")
    public ResponseEntity<List<Message>> postMessage(@RequestBody Message message) {
        log.debug("#postMessage({})", message);
        message = messageService.addMessage(message);
        return ResponseEntity.created(URI.create(path + "/" + message.getId()))
                .body(LogMessages.addGetAndClear(message));
    }

    @GetMapping(path + "/count")
    @Rate(permits=5, timeUnit=TimeUnit.MINUTES)
    public ResponseEntity<Integer> countMessages() {
        log.debug("#countMessages()");
        return ResponseEntity.ok(messageService.countMessages());
    }

    @GetMapping(path + "/{id}")
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id !=")
    public ResponseEntity<Message> getMessage(@PathVariable("id") Long id) {
        log.debug("#getMessage({})", id);
        return messageService.getMessage(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.request.header = {X-SAMPLE-TRIGGER = true}")
    public List<Message> getMessages() {
        log.debug("#getMessages()");
        List<Message> messages = new ArrayList<>();
        messages.addAll(messageService.getMessages());
        messages.addAll(LogMessages.getAndClear());
        return messages;
    }

    @DeleteMapping(path + "/{id}")
    @Rate(permits = 2, timeUnit = TimeUnit.MINUTES)
    public boolean deleteMessage(@PathVariable Long id) {
        log.debug("#deleteMessage({})", id);
        return messageService.removeMessage(id);
    }

    @PutMapping(path)
    public boolean putMessage(@RequestBody Message message) {
        log.debug("#putMessage({})", message);
        return messageService.updateMessage(message);
    }
}