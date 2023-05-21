package io.github.poshjosh.ratelimiter.tests.server;

import org.springframework.boot.CommandLineRunner;

public class InitialMessagesPersister implements CommandLineRunner {
    private final MessageService messageService;
    public InitialMessagesPersister(MessageService messageService) {
        this.messageService = messageService;
    }
    @Override
    public void run(String... args) {
        final String prefix = "InitialMessage";
        for(int i = 0; i < 3; i++) {
            addMessage(prefix + i);
        }
    }
    private void addMessage(String text) {
        Message message = new Message();
        message.setText(text);
        messageService.addMessage(message);
    }
}
