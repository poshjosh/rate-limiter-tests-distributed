package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;

import java.math.BigDecimal;
import java.util.Objects;

public final class Usage {
    public static final Usage ZERO = new Usage(MathUtil.ZERO, MathUtil.ZERO);
    private final BigDecimal time;
    private final BigDecimal memory;
    public Usage(String time, String memory) {
        this(new BigDecimal(time), new BigDecimal(memory));
    }
    public Usage(BigDecimal time, BigDecimal memory) {
        this.time = Objects.requireNonNull(time);
        this.memory = Objects.requireNonNull(memory);
    }
    public Usage add(Usage other) {
        return new Usage(time.add(other.time), memory.add(other.memory));
    }
    public Usage subtract(Usage other) {
        return new Usage(time.subtract(other.time), memory.subtract(other.memory));
    }
    public BigDecimal getTime() {
        return time;
    }
    public BigDecimal getMemory() {
        return memory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Usage usage = (Usage) o;
        return time.equals(usage.time) && memory.equals(usage.memory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, memory);
    }

    @Override public String toString() {
        return "Usage{" + "time=" + time + ", memory=" + memory + '}';
    }
}
