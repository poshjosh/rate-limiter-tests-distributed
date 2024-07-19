package io.github.poshjosh.ratelimiter.tests.server.services;

import io.github.poshjosh.ratelimiter.tests.server.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageService {

    public static class UpdateNotSupportedException extends MessageException{
        public UpdateNotSupportedException() {
            super("Update not supported");
        }
    }
    public static class InvalidRequestException extends MessageException{
        public InvalidRequestException() {
            this("");
        }
        public InvalidRequestException(String request) {
            super("Invalid request: " + request);
        }
    }
    public abstract static class MessageException extends RuntimeException{
        protected MessageException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(MessageService.class);

    private static final Map<Long, Message> messages = new ConcurrentHashMap<>();

    public Message addMessage(Message message) {
        log.debug("#addMessage({})", message);
        if (message == null) {
            throw new InvalidRequestException("message");
        }
        if (message.getId() != null) {
            throw new InvalidRequestException("message#id");
        }
        synchronized (messages) {
            final Long id = Long.valueOf(messages.size());
            message.setId(id);
            messages.put(message.getId(), message);
            log.debug("#addMessage({}), result = {}", id, message);
            return message;
        }
    }

    public int countMessages() {
        log.debug("#countMessages()");
        synchronized (messages) {
            int result = messages.size();
            log.debug("#countMessages(), result = {}", result);
            return result;
        }
    }

    public Optional<Message> getMessage(@PathVariable("id") Long id) {
        log.debug("#getMessage({})", id);
        if (id == null) {
            throw new InvalidRequestException("id");
        }
        synchronized (messages) {
            Optional<Message> result = Optional.of(messages.get(id));
            log.debug("#getMessage({}), result = {}", id, result);
            return result;
        }
    }

    public List<Message> getMessages() {
        log.debug("#getMessages()");
        synchronized (messages) {
            List<Message> result = Collections.unmodifiableList(new ArrayList<>(messages.values()));
            log.debug("#getMessages(), result = {}", result);
            return result;
        }
    }

    public boolean removeMessage(Long id) {
        log.debug("#removeMessage({})", id);
        if (id == null) {
            throw new InvalidRequestException("id");
        }
        synchronized (messages) {
            final boolean result = messages.remove(id) != null;
            log.debug("#removeMessage({}), result = {}", id, result);
            return result;
        }
    }

    public boolean updateMessage(Message message) {
        log.debug("#updateMessage({})", message);
        throw new UpdateNotSupportedException();
    }
}
