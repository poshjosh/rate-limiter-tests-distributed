package io.github.poshjosh.ratelimiter.tests.user;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(UserServiceApplication.APPLICATION_PATH)
public class UserServiceApplication extends Application {
    public static final String APPLICATION_PATH = "/api/v1";
}