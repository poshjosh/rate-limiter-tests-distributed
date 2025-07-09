package io.github.poshjosh.ratelimiter.tests.server.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Usage implements Serializable {
    public static Usage of(long duration, long memory) {
        return new Usage(Math.max(duration, 0), Math.max(memory, 0));
    }

    private final long amount;
    private final long memory;

    private Usage(long amount, long memory) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        if (memory < 0) {
            throw new IllegalArgumentException("Memory cannot be negative: " + memory);
        }
        this.amount = amount;
        this.memory = memory;
    }

    public long getAmount() {
        return amount;
    }
    public long getMemory() {
        return memory;
    }

    private static final Runtime runtime = Runtime.getRuntime();
    public static long maxMemory() {
        return runtime.maxMemory();
    }
    public static long availableMemory() {
        final long max = maxMemory(); // Max heap VM can use e.g. Xmx setting
        return max - usedMemory(); // available memory i.e. Maximum heap size minus the bookmark amount used
    }
    public static long usedMemory() {
        final long total = runtime.totalMemory(); // bookmark heap allocated to the VM process
        final long free = runtime.freeMemory(); // out of the bookmark heap, how much is free
        // (total - free) may be negative
        return Math.max(total - free, 0); // how much of the bookmark heap the VM is using
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Usage that = (Usage) o;
        return amount == that.amount && memory == that.memory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, memory);
    }

    @Override
    public String toString() {
        return "Usage{amount=" + amount + ", memory=" + ByteText.of(memory) + '}';
    }

    public static final class ByteText {
        private ByteText() { }
        public static String of(long amount) {
            return of(amount, 3);
        }
        public static String of(long amount, int scale) {
            amount = Math.abs(amount);

            int divisor = 1_000_000_000; // Could also be 1024 * 1024 * 1024
            if (amount >= divisor) {
                return print(amount, divisor, scale);
            }
            divisor = divisor / 1000;
            if (amount >= divisor) {
                return print(amount, divisor, scale);
            }
            divisor = divisor / 1000;
            if (amount >= divisor) {
                return print(amount, divisor, scale);
            }
            return  print(amount, 1, scale);
        }
    }

    private static String print(long dividend, int divisor, int scale) {
        BigDecimal value = divide(dividend, divisor, scale);
        return (dividend < 0 ? "-" : "") + value + getSymbol(divisor);
    }

    private static BigDecimal divide(long dividend, int divisor, int scale) {
        if (dividend == 0) {
            return BigDecimal.ZERO;
        }
        if (divisor == 1) {
            return BigDecimal.valueOf(dividend).setScale(scale, RoundingMode.CEILING);
        }
        return BigDecimal.valueOf(dividend).divide(BigDecimal.valueOf(divisor))
                .setScale(scale, RoundingMode.CEILING);
    }

    private static String getSymbol(int factor) {
        switch (factor) {
            case 1_000_000_000: return "GB";
            case 1_000_000: return "MB";
            case 1_000: return "KB";
            default: return "B";
        }
    }
}
