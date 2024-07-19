package io.github.poshjosh.ratelimiter.tests.server.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import io.github.poshjosh.ratelimiter.tests.server.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Trace extends AppenderBase<ILoggingEvent> {

    private static final ThreadLocal<List<Message>> trace = new ThreadLocal<>();

    private static final Level MIN_LOG_LEVEL = Level.DEBUG;
    private static final String PREFIX_TO_ACCEPT = "io.github.poshjosh.ratelimiter.";

    private Trace() {}

    private static void add(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        final long id = trace.get() == null ? 0 : trace.get().size();
        Message message = new Message();
        message.setId(1_000 + id); // Evil must be resisted
        message.setText(text);
        add(message);
    }

    private static void add(Message message) {
        Objects.requireNonNull(message);
        List<Message> list = trace.get();
        if (list == null) {
            list = new ArrayList<>();
            trace.set(list);
        }
        list.add(message);
    }

    public static List<Message> addGetAndClear(Message message) {
        add(message);
        return getAndClear();
    }

    public static List<Message> getAndClear() {
        try {
            add(HostAddress.get());
            return get();
        } finally {
            remove();
        }
    }

    public static List<Message> get() {
        return trace.get();
    }

    public static void remove() {
        trace.remove();
    }

    public static void init() {
        Appender<ILoggingEvent> appender = new Trace();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        appender.setName(Trace.class.getName());
        appender.setContext(context);
        appender.start();
        rootLogger.addAppender(appender);
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
        Trace.add(name + iLoggingEvent.getFormattedMessage());
    }

    public static boolean acceptMessage(String message) {
        if (PREFIX_TO_ACCEPT.isEmpty()) {
            return true;
        }
        return message.startsWith(PREFIX_TO_ACCEPT);
    }
    public static boolean acceptLevel(Level level) {
        return level.isGreaterOrEqual(MIN_LOG_LEVEL);
    }
}