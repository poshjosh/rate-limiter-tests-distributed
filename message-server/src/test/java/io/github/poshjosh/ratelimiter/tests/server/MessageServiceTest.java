package io.github.poshjosh.ratelimiter.tests.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MessageServer.class)
class MessageServiceTest {

    @Autowired CacheManager cacheManager;

    @Autowired MessageService messageService;

    @BeforeEach
    public void setup() {
        cacheManager.getCache(MessageService.CACHE_NAME).clear();
    }

    @Test
    void givenMessageThatShouldBeCached_whenGetById_resultShouldBeCached() {
        Message message = new Message();
        message.setText("test-message-text");
        message = messageService.addMessage(message);
        assertEquals(messageService.getMessage(message.getId()), getCachedMessage(message.getId()));
    }

    private Optional<Message> getCachedMessage(Long id) {
        return Optional.ofNullable(
                cacheManager.getCache(MessageService.CACHE_NAME)).map(c -> c.get(id, Message.class));
    }
}