package io.github.poshjosh.ratelimiter.tests.server.util.logging;

import io.github.poshjosh.ratelimiter.tests.server.model.Message;
import io.github.poshjosh.ratelimiter.tests.server.util.HostAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LogMessages {
    private static final ThreadLocal<List<Message>> messages = new ThreadLocal<>();

    private LogMessages() { }

    public static void add(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        final long id = messages.get() == null ? 0 : messages.get().size();
        Message message = new Message();
        message.setId(1_000 + id); // Evil must be resisted
        message.setText(text);
        add(message);
    }

    static void add(Message message) {
        Objects.requireNonNull(message);
        List<Message> list = messages.get();
        if (list == null) {
            list = new ArrayList<>();
            messages.set(list);
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
        return messages.get();
    }

    public static void remove() {
        messages.remove();
    }
}
