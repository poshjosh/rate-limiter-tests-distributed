package io.github.poshjosh.ratelimiter.tests.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Trace extends AppenderBase<ILoggingEvent> {

    private static final ThreadLocal<List<Message>> trace = new ThreadLocal<>();

    private static final String prefixToAccept = "io.github.poshjosh.ratelimiter.";

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
            add(Startup.getHostAddress());
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
        String loggerName = iLoggingEvent.getLoggerName();
        if (!accept(loggerName)) {
            return;
        }
        int index = loggerName.lastIndexOf('.');
        String name = index == -1 ? loggerName : loggerName.substring(index + 1);
        Trace.add(name + iLoggingEvent.getFormattedMessage());
    }

    public static boolean accept(String message) {
        if (prefixToAccept.isEmpty()) {
            return true;
        }
        return message.startsWith(prefixToAccept);
    }
}