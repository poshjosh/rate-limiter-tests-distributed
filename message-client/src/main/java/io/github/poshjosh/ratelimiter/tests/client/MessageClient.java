package io.github.poshjosh.ratelimiter.tests.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MessageClient {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MessageClient.class, args);
        Startup.log(ctx.getEnvironment());
    }
}