package io.github.poshjosh.ratelimiter.tests.server.util.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingUtil {
    private LoggingUtil() { }

    public static void addAppenderToRootLogger(Appender<ILoggingEvent> appender) {
        addAppenderToLogger(appender, Logger.ROOT_LOGGER_NAME);
    }

    public static void addAppenderToLogger(Appender<ILoggingEvent> appender, String loggerName) {
        ch.qos.logback.classic.Logger logger = getLogger(loggerName);
        appender.setName(appender.getClass().getName());
        appender.setContext(logger.getLoggerContext());
        appender.start();
        logger.addAppender(appender);
    }

    public static ch.qos.logback.classic.Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static ch.qos.logback.classic.Logger getLogger(String loggerName) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(loggerName);
    }
}
