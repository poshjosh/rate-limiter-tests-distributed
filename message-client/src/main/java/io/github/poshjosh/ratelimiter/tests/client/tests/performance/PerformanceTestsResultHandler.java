package io.github.poshjosh.ratelimiter.tests.client.tests.performance;

import io.github.poshjosh.ratelimiter.tests.client.util.Html;
import io.github.poshjosh.ratelimiter.tests.client.util.FileUtil;
import io.github.poshjosh.ratelimiter.tests.client.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PerformanceTestsResultHandler {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTestsResultHandler.class);

    private final Path dir;

    private final String filename;

    private final RateComputer rateComputer;

    public PerformanceTestsResultHandler(RateComputer rateComputer, Path dir, String filename) {
        this.dir = Objects.requireNonNull(dir);
        this.filename = Objects.requireNonNull(filename);
        this.rateComputer = Objects.requireNonNull(rateComputer);
    }

    public String process(Map statsBefore, List<Usage> usageList, Map statsAfter, List<Usage> usageRate) {

        try {
            Files.createDirectories(dir);
            log.info("Successfully created dir: {}", dir);
        }catch(IOException e) {
            log.warn("Failed to create dir: " + dir, e);
        }

        final String summary = createSummaryHtml(statsBefore, usageList, statsAfter);

        saveToFile(dir.resolve(filename + "-summary.html"),
                summary.getBytes(StandardCharsets.UTF_8));

        save(usageList, "-usage-responses");

        saveUsagePerSecond(usageList);

        saveDeviatedFromMin(usageList);

        save(usageRate, "-usage-rate");

        // return toString(usageList, "<br/>");
        return summary;
    }

    private Optional<Path> save(List<Usage> usageList, String suffix) {
        return saveToFile(dir.resolve(filename + suffix + ".csv"),
                toString(usageList, "\n").getBytes(StandardCharsets.UTF_8));
    }

    private Optional<Path> saveUsagePerSecond(List<Usage> usageList) {
        List<Usage> computed = rateComputer.computeUsagePerSecond(usageList);
        return saveTo(computed, filename + "-usage-per-sec.csv");
    }

    private Optional<Path> saveDeviatedFromMin(List<Usage> usageList) {
        List<Usage> deviatedFromMin = deviateFromMin(usageList);
        return saveTo(deviatedFromMin, filename + "-deviated-from-min.csv");
    }

    private Optional<Path> saveTo(List<Usage> usageList, String filename) {
        return saveToFile(dir.resolve(filename),
                toString(usageList, "\n").getBytes(StandardCharsets.UTF_8));
    }

    private String createSummaryHtml(Map statsBefore, List<Usage> usageList, Map statsAfter) {
        final Html html = Html.of("Summary")
                .p("Total results: " + usageList.size());
        appendMemoryStats(usageList, html);
        appendTimeStats(usageList, html);

        html.append("<h3>Stats</h3>")
                .tag("b", "Before running performance tests").table(statsBefore)
                .append("<br/>").tag("b", "After running performance tests").table(statsAfter);

        return html.toString();
    }

    private void appendMemoryStats(List<Usage> usageList, Html html) {
        if (usageList.isEmpty()) {
            html.p("Memory difference: none")
                    .p("Average memory: none");
            return;
        }
        BigDecimal min = findMinAndMax(usageList, Usage::getMemory)[0];
        BigDecimal max = findMinAndMax(usageList, Usage::getMemory)[1];
        BigDecimal diff = MathUtil.subtract(max, min);
        BigDecimal memorySum = sum(usageList).getMemory();
        BigDecimal averageMemory = MathUtil.divide(memorySum, BigDecimal.valueOf(usageList.size()));
        html.p("Memory difference: " + diff + "MB")
                .p("Average memory: " + averageMemory + "MB");
    }

    private void appendTimeStats(List<Usage> usageList, Html html) {
        BigDecimal min = findMinAndMax(usageList, Usage::getTime)[0];
        BigDecimal max = findMinAndMax(usageList, Usage::getTime)[1];
        BigDecimal diff = MathUtil.subtract(max, min);
        html.p("Time spent: " + diff + " seconds");
    }

    private String toString(List<Usage> usageList, String lineSeparator) {
        StringBuilder result = new StringBuilder();
        usageList.forEach(usage -> {
            result.append(lineSeparator).append(usage.getTime()).append(", ").append(usage.getMemory());
        });
        return result.toString();
    }

    private Optional<Path> saveToFile(Path path, byte [] bytes) {
        return FileUtil.save(path, bytes);
    }

    private List<Usage> deviateFromMin(List<Usage> candidates) {
        BigDecimal minTime = findMinAndMax(candidates, Usage::getTime)[0];
        BigDecimal minMemory = findMinAndMax(candidates, Usage::getMemory)[0];
        Usage min = new Usage(minTime, minMemory);
        return candidates.stream()
                .map(usage -> usage.subtract(min))
                .collect(Collectors.toList());
    }

    private BigDecimal [] findMinAndMax(List<Usage> candidates, Function<Usage, BigDecimal> converter) {
        BigDecimal [] result = new BigDecimal[]{MathUtil.ZERO, MathUtil.ZERO};
        for (Usage usage : candidates) {
            final BigDecimal value = converter.apply(usage);
            if (value.compareTo(result[0]) < 0) {
                result[0] = value;
            }
            if (value.compareTo(result[1]) > 0) {
                result[1] = value;
            }
        }
        return result;
    }

    private Usage sum(List<Usage> usageList) {
        Usage result = Usage.ZERO;
        for (Usage usage : usageList) {
            result = result.add(usage);
        }
        return new Usage(MathUtil.setScale(result.getTime()), MathUtil.setScale(result.getMemory()));
    }
}
