package io.github.poshjosh.ratelimiter.tests.server.util.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public final class LogMessageCollector extends AppenderBase<ILoggingEvent> {
    private static final Level MIN_LOG_LEVEL = Level.DEBUG;
    private static final String PREFIX_TO_ACCEPT = "io.github.poshjosh.ratelimiter.";

    private LogMessageCollector() {}


    public static void init() {
        LoggingUtil.addAppenderToRootLogger(new LogMessageCollector());
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (!acceptLevel(iLoggingEvent.getLevel())) {
            return;
        }
        String loggerName = iLoggingEvent.getLoggerName();
        if (!acceptMessage(loggerName)) {
            return;
        }
        int index = loggerName.lastIndexOf('.');
        String name = index == -1 ? loggerName : loggerName.substring(index + 1);
        LogMessages.add(name + iLoggingEvent.getFormattedMessage());
    }

    private static boolean acceptMessage(String message) {
        return PREFIX_TO_ACCEPT.isEmpty() || message.startsWith(PREFIX_TO_ACCEPT);
    }
    private static boolean acceptLevel(Level level) {
        return level.isGreaterOrEqual(MIN_LOG_LEVEL);
    }
}