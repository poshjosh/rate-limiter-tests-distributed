package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import java.util.Objects;

public class PerformanceTestData {
    private int limit = 25;
    private int timeout = 0;
    private int work = 100;
    private int percent = 100;
    private int iterations = 1;
    private int durationPerTestUser = 20;

    private RequestSpreadType requestSpreadType = RequestSpreadType.STEEP_GAUSSIAN_5;
    public PerformanceTestData() { }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getDurationPerTestUser() {
        return durationPerTestUser;
    }

    public void setDurationPerTestUser(int durationPerTestUser) {
        this.durationPerTestUser = durationPerTestUser;
    }

    public RequestSpreadType getRequestSpreadType() {
        return requestSpreadType;
    }

    public void setRequestSpreadType(RequestSpreadType requestSpreadType) {
        this.requestSpreadType = requestSpreadType;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PerformanceTestData that = (PerformanceTestData) o;
        return limit == that.limit && timeout == that.timeout && work == that.work
                && percent == that.percent && iterations == that.iterations
                && durationPerTestUser == that.durationPerTestUser
                && requestSpreadType == that.requestSpreadType;
    }

    @Override public int hashCode() {
        return Objects.hash(limit, timeout, work, percent, iterations, durationPerTestUser,
                requestSpreadType);
    }

    @Override public String toString() {
        return "PerformanceTestData{" + "limit=" + limit + ", timeout=" + timeout + ", work="
                + work + ", factor=" + percent + ", iterations=" + iterations
                + ", durationPerTestUser=" + durationPerTestUser
                + ", requestSpreadType=" + requestSpreadType + '}';
    }
}
