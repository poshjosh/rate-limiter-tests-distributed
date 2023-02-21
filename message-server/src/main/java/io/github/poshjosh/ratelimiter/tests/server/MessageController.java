package io.github.poshjosh.ratelimiter.tests.server;

import io.github.poshjosh.ratelimiter.annotations.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class MessageController implements ErrorController {

    private final Logger log = LoggerFactory.getLogger(MessageController.class);

    private static final String path = "/messages";

    private final MessageService messageService;
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @RequestMapping("/error")
    public List<Message> error(HttpServletRequest request) {
        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final String sval = oval == null ? "" : oval.toString();
        log.warn(sval);
        return Trace.getAndClear();
    }

    @GetMapping
    public String home() {
        return "Message Service";
    }

    @PostMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id!=")
    public ResponseEntity<List<Message>> postMessage(@RequestBody Message message) {
        log.info("#postMessage({})", message);
        message = messageService.addMessage(message);
        return ResponseEntity.created(URI.create(path + "/" + message.getId()))
                .body(Trace.addGetAndClear(message));
    }

    @PostMapping(path + "/count")
    @Rate(permits=5, timeUnit=TimeUnit.MINUTES)
    public ResponseEntity<Integer> countMessages() {
        log.info("#countMessages()");
        return ResponseEntity.ok(messageService.countMessages());
    }

    @GetMapping(path + "/{id}")
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.session.id!=")
    public ResponseEntity<Message> getMessage(@PathVariable("id") Long id) {
        log.info("#getMessage({})", id);
        return messageService.getMessage(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path)
    @Rate(permits=1, timeUnit=TimeUnit.MINUTES, when="web.request.header={X-SAMPLE-TRIGGER=true}")
    public List<Message> getMessages() {
        log.info("#getMessages()");
        List<Message> messages = new ArrayList<>();
        messages.addAll(messageService.getMessages());
        messages.addAll(Trace.getAndClear());
        return messages;
    }

    @DeleteMapping(path + "/{id}")
    @Rate(permits = 2, timeUnit = TimeUnit.MINUTES)
    public boolean deleteMessage(@PathVariable Long id) {
        log.info("#deleteMessage({})", id);
        return messageService.removeMessage(id);
    }

    @PutMapping(path)
    public boolean putMessage(@RequestBody Message message) {
        log.info("#putMessage({})", message);
        return messageService.updateMessage(message);
    }
}