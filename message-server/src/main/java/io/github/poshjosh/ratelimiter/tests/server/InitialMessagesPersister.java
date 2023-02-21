package io.github.poshjosh.ratelimiter.tests.server;

import org.springframework.boot.CommandLineRunner;

public class InitialMessagesPersister implements CommandLineRunner {
    private final MessageService messageService;
    public InitialMessagesPersister(MessageService messageService) {
        this.messageService = messageService;
    }
    @Override
    public void run(String... args) {
        for(int i = 0; i < 10; i++) {
            addMessage("InitialMessage"+i);
        }
    }
    private void addMessage(String text) {
        Message message = new Message();
        message.setText(text);
        messageService.addMessage(message);
    }
}
