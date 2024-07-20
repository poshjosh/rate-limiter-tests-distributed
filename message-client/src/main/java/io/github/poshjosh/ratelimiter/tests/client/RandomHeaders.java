package io.github.poshjosh.ratelimiter.tests.client;

import org.springframework.http.HttpHeaders;

public final class RandomHeaders {
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String USER_AGENT_HEADER = "User-Agent";
    static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private RandomHeaders() { }

    public static void randomize(HttpHeaders headers) {
        final float probabilityOfBeingLoggedIn = 0.5f;
        final float probabilityOfBeingABot = 0.1f;
        randomize(headers, probabilityOfBeingLoggedIn, probabilityOfBeingABot);
    }

    //@VisibleForTesting
    static void randomize(HttpHeaders headers, float probabilityOfBeingLoggedIn, float probabilityOfBeingABot) {
        final String ip = RandomIps.generate(225, 30); // min = 225, max = min + 30
        final RandomCredentials.Credentials credentials = RandomCredentials
                .getOrGenerate(ip, probabilityOfBeingLoggedIn).orElse(null);
        probabilityOfBeingABot = Math.min(1.0f, probabilityOfBeingABot / probabilityOfBeingLoggedIn);
        final String ua = RandomUserAgents
                .getOrGenerate(ip, credentials != null ? 0 : probabilityOfBeingABot);
        headers.set(X_FORWARDED_FOR_HEADER, ip);
        if (credentials != null) {
            headers.set(AUTHORIZATION_HEADER, "Basic " + credentials.base64Encoded());
        }
        headers.set(USER_AGENT_HEADER, ua);
    }

    //@VisibleForTesting
    static boolean isRegisteredBot(String userAgent) {
        return RandomUserAgents.isRegisteredBot(userAgent);
    }

    //@VisibleForTesting
    static void reset() {
        RandomUserAgents.reset();
        RandomCredentials.reset();
    }
}
