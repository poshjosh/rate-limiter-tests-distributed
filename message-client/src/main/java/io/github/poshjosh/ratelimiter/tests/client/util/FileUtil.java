package io.github.poshjosh.ratelimiter.tests.client.util;

import io.github.poshjosh.ratelimiter.tests.client.performance.PerformanceTestData;
import io.github.poshjosh.ratelimiter.tests.client.performance.RateLimitMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private FileUtil() {}

//    public static void main(String... args) {
//        final Path path = java.nio.file.Paths.get(System.getProperty("user.home") +
//                        "/Library/Application Support/JetBrains/IntelliJIdea2022.3/scratches/rate-limiter/logs/tests/performance/4/0_auto-25-timeout-0-steady_1-work-50-percent-100-iterations-1-user-duration-20-A.csv")
//                .toAbsolutePath().normalize();
//        System.out.println("          File: " + path);
//        if (Files.exists(path)) {
//            System.out.println("Next file path: " + nextFileName(path));
//        } else {
//            System.out.println("Does not exist: " + path);
//        }
//    }

    public static Optional<Path> save(Path path, byte [] bytes) {
        if (Files.exists(path)) {
            path = nextFileName(path);
        }
        try {
            path = Files.write(path, bytes, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Successfully saved {} bytes to: {}", bytes.length, path);
            return Optional.of(path);
        } catch (IOException e) {
            log.warn("Failed to write " + bytes.length + " bytes to " + path, e);
            return Optional.empty();
        }
    }

    private static Path nextFileName(Path path) {
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("Expected a file. Found directory: " + path);
        }
        final char separator = '_';
        final Path dir = path.getParent();
        final String fname = path.getFileName().toString();
        if (!Character.isDigit(fname.charAt(0)) || fname.indexOf(separator) == -1) {
            return dir.resolve("0" + separator + fname);
        }
        final String nextIndex = String.valueOf(fileIndex(dir, separator) + 1);
        //System.out.println("Next index: " + nextIndex);
        if (fname.startsWith(nextIndex)) {
            return path;
        }
        return dir.resolve(nextIndex + fname.substring(fname.indexOf(separator)));
    }

    private static int fileIndex(Path dir, char separator) {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Expected a directory. Found file: " + dir);
        }
        try {
            List<String> fnames = Files.list(dir)
                    .filter(path -> !Files.isDirectory(path))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(s -> Character.isDigit(s.charAt(0)))
                    .filter(s -> s.indexOf(separator) != -1)
                    .collect(Collectors.toList());
            for (String fname : fnames) {
                try {
                    return Integer.parseInt(fname.substring(0, fname.indexOf(separator)));
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException e) {
            log.warn("Failed to list dir: " + dir, e);
        }
        return -1;
    }

    public static String buildFilename(RateLimitMode rateLimitMode, PerformanceTestData data) {
        StringBuilder b = new StringBuilder();
        b.append(rateLimitMode.name().toLowerCase());
        if (!RateLimitMode.Off.equals(rateLimitMode)) {
            b.append('-').append(data.getLimit()).append("-timeout-").append(data.getTimeout());
        }
        return b.append('-').append(data.getRequestSpreadType().toString().toLowerCase())
                .append("-work-").append(data.getWork())
                .append("-percent-").append(data.getPercent())
                .append("-iterations-").append(data.getIterations())
                .append("-user-duration-").append(data.getDurationPerTestUser()).toString();
    }
}
