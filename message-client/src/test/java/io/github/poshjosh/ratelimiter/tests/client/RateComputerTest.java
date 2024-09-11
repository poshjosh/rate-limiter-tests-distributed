package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.tests.performance.RateComputer;
import io.github.poshjosh.ratelimiter.tests.client.tests.performance.Usage;
import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RateComputerTest {

    private final boolean debug = true;

    private final RateComputer rateComputer = new RateComputer();

    static List<Usage> [][] args() {
        return new List[][] {
                new List[]{
                        Arrays.asList(new Usage("1", "0"), new Usage("2", "0"), new Usage("3", "0")),
                        Arrays.asList(new Usage("1.00", "0.00"), new Usage("1.00", "0.00"), new Usage("1.00", "0.00"))},
                new List[]{
                        Arrays.asList(new Usage("1", "0"), new Usage("2", "0"), new Usage("4", "0")),
                        Arrays.asList(new Usage("1.00", "0.00"), new Usage("1.00", "0.00"), new Usage("0.50", "0.00"), new Usage("0.50", "0.00"))},
                new List[]{
                        Arrays.asList(new Usage("1", "0"), new Usage("1", "0"), new Usage("2", "0")),
                        Arrays.asList(new Usage("2.00", "0.00"), new Usage("1.00", "0.00"))},
                new List[]{
                        Arrays.asList(new Usage("1.1111111", "0"), new Usage("1", "0"), new Usage("1.9999999", "0"), new Usage("9.9999999", "0")),
                        Arrays.asList(new Usage("3.00", "0.00"), new Usage("0.13", "0.00"), new Usage("0.13", "0.00")
                                , new Usage("0.13", "0.00"), new Usage("0.13", "0.00"), new Usage("0.13", "0.00")
                                , new Usage("0.13", "0.00"), new Usage("0.13", "0.00"), new Usage("0.13", "0.00")
                        )},

                new List[]{
                        Arrays.asList(new Usage("1", "1"), new Usage("1", "2"), new Usage("1", "3")),
                        Arrays.asList(new Usage("3.00", "2.00"))},
                new List[]{
                        Arrays.asList(new Usage("1", "1"), new Usage("2", "2"), new Usage("3", "3")),
                        Arrays.asList(new Usage("1.00", "1.00"), new Usage("1.00", "2.00"), new Usage("1.00", "3.00"))},
                new List[]{
                        Arrays.asList(new Usage("1", "1"), new Usage("2", "1"), new Usage("4", "1")),
                        Arrays.asList(new Usage("1.00", "1.00"), new Usage("1.00", "1.00"), new Usage("0.50", "0.50"), new Usage("0.50", "0.50"))},
                new List[]{
                        Arrays.asList(new Usage("1", "2"), new Usage("1", "1"), new Usage("2", "1")),
                        Arrays.asList(new Usage("2.00", "1.50"), new Usage("1.00", "1.00"))},
        };
    }

    // TODO - Fix this test, or use an external library for computation rather than RateComputer
    @Disabled
    @ParameterizedTest
    @MethodSource("args")
    void computeUsagePerSecond(List<Usage> input, List<Usage> expected) {
        List<Usage> actual = rateComputer.computeUsagePerSecond(input);
        if (debug) System.out.println("\n   Input: " + input + "\nExpected: " + expected +
                "\n  Actual: " + actual);
        assertEquals(expected, actual);
    }

    // TODO - Fix this test, or use an external library for computation rather than RateComputer
    @Disabled
    @ParameterizedTest
    @ValueSource(strings = {
            // no of inputs,                // expected results
            "3,      1,    2,    3,         1.00,1.00,1.00",
            "3,      1,    2,    4,         1.00,1.00,0.50,0.50",
            "3,      1,    1,    2,         2.00,1.00",
            "4,      1.111,1,1.999,9.0101,  3.00,0.13,0.13,0.13,0.13,0.13,0.13,0.13,0.13"
    })
    void computeRequestsPerSecond(String csv) {
        final List<String> parts = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
        //System.out.println(parts);

        final int numberOfInputs = Integer.parseInt(parts.get(0));

        List<BigDecimal> input = givenTimeSeriesInputs(parts, numberOfInputs);

        List<BigDecimal> expected = expectedResult(parts, numberOfInputs);

        List<BigDecimal> actual = fromUsageList(rateComputer.computeUsagePerSecond(toUsageList(input)));
        if(debug) System.out.println("  Actual: " + actual);

        assertEquals(expected, actual);
    }

    private List<Usage> toUsageList(List<BigDecimal> input) {
        return input.stream().map(e -> new Usage(e, MathUtil.ZERO)).collect(Collectors.toList());
    }

    private List<BigDecimal> fromUsageList(List<Usage> userList) {
        return userList.stream().map(Usage::getTime).collect(Collectors.toList());
    }

    private List<BigDecimal> givenTimeSeriesInputs(List<String> parts, int numberOfInputs) {
        final List<String> inputList = parts.subList(1, numberOfInputs + 1);
        if(debug) System.out.println("\n   Input: " + inputList);
        return inputList.stream().map(BigDecimal::new).collect(Collectors.toList());
    }

    private List<BigDecimal> expectedResult(List<String> parts, int numberOfInputs) {
        final List<String> expectedList = parts.subList(numberOfInputs + 1, parts.size());
        if(debug) System.out.println("Expected: " + expectedList);
        return expectedList.stream().map(BigDecimal::new).collect(Collectors.toList());
    }
}