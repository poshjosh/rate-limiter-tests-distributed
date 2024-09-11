package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RateComputer {

    private final boolean strict = false;

    /**
     * Compute usage per each second.
     *
     * <p>Usage comprises time and memory.</p>
     *
     * To make the computation start from zero, zero is assumed to be the first element.
     *
     * <p>Examples of computing time part of usage</p>
     * <pre>
     *  Input: 1, 2, 3          Resolved input: 0, 1, 2, 3
     * Output: 1, 1, 1          (3 secs total. Each sec = 1/s)
     *
     *  Input: 1, 2, 4          Resolved input: 0, 1, 2, 4
     * Output: 1, 1, 0.5, 0.5   (4 secs total, First 2 secs = 1/s, Last 2 secs = 0.5/sec)
     *
     *  Input: 1, 1, 2          Resolved input: 0, 1, 1, 2
     * Output: 2, 1             (2 secs total, First sec = 2/s, Second sec = 1/s)
     * </pre>
     *
     * <p>
     *     Computation of the memory part also depends on the time parts.
     *     Here are some example outputs.
     * </p>
     * <pre>
      Input: [Usage{time=1, memory=0}, Usage{time=2, memory=0}, Usage{time=3, memory=0}]
     Output: [Usage{time=1.00, memory=0.00}, Usage{time=1.00, memory=0.00}, Usage{time=1.00, memory=0.00}]

      Input: [Usage{time=1, memory=0}, Usage{time=2, memory=0}, Usage{time=4, memory=0}]
     Output: [Usage{time=1.00, memory=0.00}, Usage{time=1.00, memory=0.00}, Usage{time=0.50, memory=0.00}, Usage{time=0.50, memory=0.00}]

      Input: [Usage{time=1, memory=0}, Usage{time=1, memory=0}, Usage{time=2, memory=0}]
     Output: [Usage{time=2.00, memory=0.00}, Usage{time=1.00, memory=0.00}]

      Input: [Usage{time=1.1111111, memory=0}, Usage{time=1, memory=0}, Usage{time=1.9999999, memory=0}, Usage{time=9.9999999, memory=0}]
     Output: [Usage{time=3.00, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}, Usage{time=0.13, memory=0.00}]

      Input: [Usage{time=1, memory=1}, Usage{time=1, memory=2}, Usage{time=1, memory=3}]
     Output: [Usage{time=3.00, memory=2.00}]

      Input: [Usage{time=1, memory=1}, Usage{time=2, memory=2}, Usage{time=3, memory=3}]
     Output: [Usage{time=1.00, memory=1.00}, Usage{time=1.00, memory=2.00}, Usage{time=1.00, memory=3.00}]

      Input: [Usage{time=1, memory=1}, Usage{time=2, memory=1}, Usage{time=4, memory=1}]
     Output: [Usage{time=1.00, memory=1.00}, Usage{time=1.00, memory=1.00}, Usage{time=0.50, memory=0.50}, Usage{time=0.50, memory=0.50}]

      Input: [Usage{time=1, memory=2}, Usage{time=1, memory=1}, Usage{time=2, memory=1}]
     Output: [Usage{time=2.00, memory=1.50}, Usage{time=1.00, memory=1.00}]
     * </pre>
     *
     * @param usageList A list is a snapshot of usage (time, memory) at which a request was made
     * @return The usage adjusted to a per second rate
     */
    public List<Usage> computeUsagePerSecond(List<Usage> usageList) {
        if (usageList.isEmpty()) {
            return Collections.emptyList();
        }
        if (usageList.size() == 1) {
            return Collections.singletonList(usageList.get(0));
        }
        List<Usage> result = new ArrayList<>();
        List<Usage> repeated = new ArrayList<>();
        for(int i = 0; i < usageList.size(); i++) {
            final Usage curr = usageList.get(i);
            final Usage prev = i == 0 ? Usage.ZERO : usageList.get(i - 1);
            final int timeDiff = timeDiff(prev, curr, i);
            final int memoryDiff = memoryDiff(prev, curr);
            if (timeDiff == 0) {
                if (repeated.isEmpty()) {
                    repeated.add(prev);
                }
                repeated.add(curr);
            } else {

                updateLastWithAverageOf(result, repeated);

                BigDecimal [] timeArr = new BigDecimal[timeDiff];
                Arrays.fill(timeArr, invert(timeDiff));

                BigDecimal [] memoryArr = new BigDecimal[memoryDiff];
                BigDecimal invertedMemoryDiff = invert(memoryDiff);
                for(int j = 0; j < memoryDiff; j++) {
                    if (memoryDiff == 1) {
                        memoryArr[j] = MathUtil.setScale(curr.getMemory());
                    } else {
                        memoryArr[j] = MathUtil.multiply(curr.getMemory(), invertedMemoryDiff);
                    }
                }

                if (timeArr.length != memoryArr.length) {
                    throw new IllegalArgumentException("Error processing: " + curr);
                }

                for(int j = 0; j < timeArr.length; j++) {
                    result.add(new Usage(timeArr[j], memoryArr[j]));
                }

                repeated.clear();
            }
        }

        updateLastWithAverageOf(result, repeated);

        return Collections.unmodifiableList(result);
    }

    private void updateLastWithAverageOf(List<Usage> result, List<Usage> repeated) {
        if (repeated.isEmpty()) {
            return;
        }
        if (result.isEmpty()) {
            result.add(average(repeated));
        } else {
            result.set(result.size() -1, average(repeated));
        }
    }

    private Usage average(List<Usage> usageList) {
        return new Usage(MathUtil.toBigDecimal(usageList.size()), averageMemory(usageList));
    }

    private int timeDiff(Usage first, Usage second, int index) {
        final int currTime = second.getTime().intValue();
        final int prevTime = first.getTime().intValue();
        if (currTime < prevTime) {
            if (!strict) {
                return 0;
            }
            throw new IllegalArgumentException("Expecting sequentially increasing values but found ["
                    + index + "] = " + currTime + " to be less than [" + (index - 1) + "] = " + prevTime);
        }
        return currTime - prevTime;
    }

    private int memoryDiff(Usage first, Usage second) {
        final int currTime = second.getMemory().intValue();
        final int prevTime = first.getMemory().intValue();
        return Math.abs(currTime - prevTime);
    }

    private BigDecimal averageMemory(List<Usage> usageList) {
        BigDecimal memorySum = MathUtil.ZERO;
        for(Usage usage : usageList) {
            memorySum = MathUtil.add(memorySum, usage.getMemory());
        }
        return MathUtil.divide(memorySum, BigDecimal.valueOf(usageList.size()));
    }

    private BigDecimal invert(int value) {
        if (value == 0) {
            return MathUtil.ZERO;
        }
        if (value == 1) {
            return MathUtil.ONE;
        }
        return MathUtil.divide(MathUtil.ONE, BigDecimal.valueOf(value));
    }
}
