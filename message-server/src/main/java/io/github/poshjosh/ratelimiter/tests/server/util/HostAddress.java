package io.github.poshjosh.ratelimiter.tests.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class HostAddress {
    private static final Logger log = LoggerFactory.getLogger(HostAddress.class);

    private HostAddress() { }

    private static String hostAddress;
    public static String get() {
        if (hostAddress != null) {
            return hostAddress;
        }
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
            hostAddress = "localhost";
        }
        return hostAddress;
    }
}
