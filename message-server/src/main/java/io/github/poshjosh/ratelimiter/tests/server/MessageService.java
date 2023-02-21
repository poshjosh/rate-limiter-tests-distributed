package io.github.poshjosh.ratelimiter.tests.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@CacheConfig(cacheNames = {MessageService.CACHE_NAME})
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

    public static final String CACHE_NAME = "messages-cache";

    private static final Map<Long, Message> messages = new ConcurrentHashMap<>();

    public Message addMessage(Message message) {
        if (message == null) {
            throw new InvalidRequestException("message");
        }
        if (message.getId() != null) {
            throw new InvalidRequestException("message#id");
        }
        synchronized (messages) {
            message.setId(Long.valueOf(messages.size()));
            messages.put(message.getId(), message);
            return message;
        }
    }

    public int countMessages() {
        synchronized (messages) {
            return messages.size();
        }
    }

    @Cacheable(key = "#id")
    public Optional<Message> getMessage(@PathVariable("id") Long id) {
        if (id == null) {
            throw new InvalidRequestException("id");
        }
        log.info("#getMessage({})", id);
        return Optional.of(messages.get(id));
    }

    public List<Message> getMessages() {
        log.info("#getMessages()");
        synchronized (messages) {
            return Collections.unmodifiableList(new ArrayList<>(messages.values()));
        }
    }

    public boolean removeMessage(Long id) {
        if (id == null) {
            throw new InvalidRequestException("id");
        }
        synchronized (messages) {
            return messages.remove(id) != null;
        }
    }

    public boolean updateMessage(Message message) {
        throw new UpdateNotSupportedException();
    }
}
