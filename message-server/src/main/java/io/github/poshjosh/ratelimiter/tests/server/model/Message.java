package io.github.poshjosh.ratelimiter.tests.server.model;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private Long id;
    private String text;
    public Message() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return "Message{" + id + "=" + text + '}';
    }
}
