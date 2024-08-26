package io.github.poshjosh.ratelimiter.tests.server.resources;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import io.github.poshjosh.ratelimiter.tests.server.RemoteRateLimiter;
import io.github.poshjosh.ratelimiter.tests.server.model.Message;
import io.github.poshjosh.ratelimiter.tests.server.services.MessageService;
import io.github.poshjosh.ratelimiter.tests.server.util.logging.LogMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    public static final String path = "/messages";

    private final MessageService messageService;
    private final RemoteRateLimiter remoteRateLimiter;

    public MessageController(MessageService messageService, RemoteRateLimiter remoteRateLimiter) {
        this.messageService = messageService;
        this.remoteRateLimiter = remoteRateLimiter;
        remoteRateLimiter.addRate("messages.post", "1/m", "web.session.id !=");
        remoteRateLimiter.addRate("messages.count", "5/m", null);
        remoteRateLimiter.addRate("messages.get-one", "1/m", "web.session.id !=");
        remoteRateLimiter.addRate("messages.get-all", "1/m", "web.request.header[X-SAMPLE-TRIGGER] = true");
        remoteRateLimiter.addRate("messages.delete", "2/m", null);
    }

    @PostMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id !=")
    public ResponseEntity<List<Message>> postMessage(HttpServletRequest request, @RequestBody Message message) {
        remoteRateLimiter.checkLimit("messages.post", request);

        log.debug("#postMessage({})", message);
        message = messageService.addMessage(message);
        return ResponseEntity.created(URI.create(path + "/" + message.getId()))
                .body(LogMessages.addGetAndClear(message));
    }

    @GetMapping(path + "/count")
    @Rate(permits=5, timeUnit=TimeUnit.MINUTES)
    public ResponseEntity<Integer> countMessages(HttpServletRequest request) {
        remoteRateLimiter.checkLimit("messages.count", request);

        log.debug("#countMessages()");
        return ResponseEntity.ok(messageService.countMessages());
    }

    @GetMapping(path + "/{id}")
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id !=")
    public ResponseEntity<Message> getMessage(HttpServletRequest request, @PathVariable("id") Long id) {
        remoteRateLimiter.checkLimit("messages.get-one", request);

        log.debug("#getMessage({})", id);
        return messageService.getMessage(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.request.header[X-SAMPLE-TRIGGER] = true")
    public List<Message> getMessages(HttpServletRequest request) {
        remoteRateLimiter.checkLimit("messages.get-all", request);

        log.debug("#getMessages()");
        List<Message> messages = new ArrayList<>();
        messages.addAll(messageService.getMessages());
        messages.addAll(LogMessages.getAndClear());
        return messages;
    }

    @DeleteMapping(path + "/{id}")
    @Rate(permits = 2, timeUnit = TimeUnit.MINUTES)
    public boolean deleteMessage(HttpServletRequest request, @PathVariable Long id) {
        remoteRateLimiter.checkLimit("messages.delete", request);

        log.debug("#deleteMessage({})", id);
        return messageService.removeMessage(id);
    }

    @PutMapping(path)
    public boolean putMessage(@RequestBody Message message) {
        log.debug("#putMessage({})", message);
        return messageService.updateMessage(message);
    }
}