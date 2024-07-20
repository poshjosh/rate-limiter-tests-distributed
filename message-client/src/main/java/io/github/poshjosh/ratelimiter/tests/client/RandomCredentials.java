package io.github.poshjosh.ratelimiter.tests.client;

import java.time.Instant;
import java.util.*;

public final class RandomCredentials {
    public static final class Credentials{
        private final String username;
        private final String password;
        public Credentials(String username, String password){
            this.username = Objects.requireNonNull(username);
            this.password = Objects.requireNonNull(password);
        }
        public String base64Encoded() {
            return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        }
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Credentials that = (Credentials) o;
            return Objects.equals(username, that.username)
                    && Objects.equals(password, that.password);
        }
        @Override
        public int hashCode() {
            return Objects.hash(username, password);
        }
    }
    private static final long REFERENCE_INSTANT =
            Instant.parse("2024-07-20T12:00:00.00Z").toEpochMilli();
    private static final Map<String, Credentials> ipToCredentials = new HashMap<>();

    private RandomCredentials() { }

    /**
     * Generate a random user agent string.
     * @param remoteAddress The IP address of the client
     * @param probabilityOfBeingLoggedIn Value between 0 and 1 (inclusive), that determines
     *                                   the probability of returning a non empty optional.
     * @return The random user agent string
     */
    public static Optional<Credentials> getOrGenerate(
            String remoteAddress, float probabilityOfBeingLoggedIn) {
        final Optional<RandomCredentials.Credentials> credentialsOpt = get(remoteAddress);
        final RandomCredentials.Credentials credentials = credentialsOpt
                .orElseGet(() -> generate(probabilityOfBeingLoggedIn).orElse(null));
        if (credentials == null) {
            return Optional.empty();
        }
        ipToCredentials.put(remoteAddress, credentials);
        return Optional.of(credentials);
    }

    //@VisibleForTesting
    static void reset() {
        ipToCredentials.clear();
    }

    /**
     * Get a previously generated random credentials object.
     * @param remoteAddress The IP address of the client
     * @return The optional random credentials object.
     */
    private static Optional<Credentials> get(String remoteAddress) {
        Credentials credentials = ipToCredentials.get(remoteAddress);
        return Optional.ofNullable(credentials);
    }

    /**
     * Generate a random credentials object.
     * @param probabilityOfBeingLoggedIn Value between 0 and 1 (inclusive), that determines
     *                                   the probability of returning a non-empty optional.
     * @return The optional random credentials object.
     */
    private static Optional<Credentials> generate(float probabilityOfBeingLoggedIn) {
        if (Math.random() >= probabilityOfBeingLoggedIn){
            return Optional.empty();
        }
        final long curr = System.currentTimeMillis();
        final long diff = curr - REFERENCE_INSTANT;
        return Optional.of(new Credentials(
                "user_" + Long.toHexString(diff), Long.toHexString(curr)));
    }
}
