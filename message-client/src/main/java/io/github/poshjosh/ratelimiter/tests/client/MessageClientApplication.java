package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.util.EnvLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessageClientApplication {
    public static void main(String[] args) {
        EnvLogger.log(SpringApplication.run(MessageClientApplication.class, args).getEnvironment());
    }
}