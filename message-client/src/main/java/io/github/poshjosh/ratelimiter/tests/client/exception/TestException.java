package io.github.poshjosh.ratelimiter.tests.client.exception;

public class TestException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public static TestException alreadyRunning() {
        return new TestException("Test already running.");
    }
    public static TestException cacheProblem() {
        return new TestException("Cache may not be working as expected.");
    }
    public static TestException interrupted() {
        return new TestException("Test(s) were interrupted.");
    }

    public TestException() { }

    public TestException(String message) {
        super(message);
    }
}
