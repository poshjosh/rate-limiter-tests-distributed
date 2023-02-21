package io.github.poshjosh.ratelimiter.tests.user;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(UserService.APPLICATION_PATH)
public class UserService extends Application {
    public static final String APPLICATION_PATH = "/api/v1";
}