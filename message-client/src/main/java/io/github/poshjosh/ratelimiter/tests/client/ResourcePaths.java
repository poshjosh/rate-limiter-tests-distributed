package io.github.poshjosh.ratelimiter.tests.client;

import java.net.URI;
import java.util.Objects;

public interface ResourcePaths {
    String USAGE_PATH = "/usage";
    String USAGE_SUMMARY_PATH = USAGE_PATH + "/summary";
    String MESSAGE_PATH = "/messages";
    String TESTS_PATH = MESSAGE_PATH + "/tests";
    String PERFORMANCE_TESTS_PATH = TESTS_PATH + "/performance";

    static URI performanceTestUri(URI base, int limit, int timeout, int work) {
        Objects.requireNonNull(base);
        final String subPath = "/limited/" + limit;
        final String query = "?timeout=" + timeout + "&work=" + work;
        return base.resolve(USAGE_PATH + subPath + query);
    }
}
