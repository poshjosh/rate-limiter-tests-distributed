package io.github.poshjosh.ratelimiter.tests.client.util;

import io.github.poshjosh.ratelimiter.tests.client.resources.ResourcePaths;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class Html {
    public static Html of(String title) {
        return of(title, "");
    }
    public static Html of(String title, Object content) {
        return new Html(title, content);
    }
    public static StringBuilder a(String link, Object text, StringBuilder appendTo) {
        Objects.requireNonNull(link);
        Objects.requireNonNull(text);
        return appendTo.append("<a href=\"").append(link).append("\">").append(text).append("</a>");
    }
    public static StringBuilder h1(Object content, StringBuilder appendTo) {
        return tag("h1", content, appendTo);
    }
    public static StringBuilder h3(Object content, StringBuilder appendTo) {
        return tag("h3", content, appendTo);
    }
    public static StringBuilder p(Object content, StringBuilder appendTo) {
        return tag("p", content, appendTo);
    }
    public static StringBuilder table(Map data, StringBuilder appendTo) {
        appendTo.append("<table style=\"border:1px solid black;\">");
        final String TD = "<td  style=\"border:1px solid black; padding:0.5rem;\">";
        final String _END = "</td></tr>";
        if (data == null) {
            return appendTo.append("<tr>").append(TD).append("null").append(_END);
        } else if (data.isEmpty()) {
            return appendTo.append("<tr>").append(TD).append("No data. ")
                    .append("<a href=\"")
                    .append(ResourcePaths.USAGE_SUMMARY_PATH)
                    .append("\">Request usage again</a>")
                    .append(_END);
        } else{
            data.forEach((k, v) -> {
                appendTo.append("<tr>").append(TD).append(k).append("</td>").append(TD).append(v).append(_END);
            });
        }
        return appendTo.append("</table>");
    }
    public static StringBuilder tag(String tagName, Object content, StringBuilder appendTo) {
        return tag(tagName, null, null, content, appendTo);
    }
    public static StringBuilder tag(String tagName, String attrName, Object attrValue, Object content) {
        return tag(tagName, attrName, attrValue, content, new StringBuilder());
    }
    public static StringBuilder tag(String tagName, String attrName, Object attrValue, Object content, StringBuilder appendTo) {
        Map attributes = attrName == null ? Collections.emptyMap() :
                Collections.singletonMap(attrName, attrValue);
        return tag(tagName, attributes, content, appendTo);
    }
    public static StringBuilder tag(String tagName, Map attributes, Object content, StringBuilder appendTo) {
        Objects.requireNonNull(tagName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(attributes);
        appendTo.append('<').append(tagName);
        attributes.forEach((k, v) -> appendTo.append(' ').append(k).append("=\"").append(v).append("\""));
        return appendTo.append('>').append(content)
                .append('<').append('/').append(tagName).append('>');
    }
    private final StringBuilder title = new StringBuilder();
    private final StringBuilder body = new StringBuilder();
    private Html(String title, Object body) {
        this.title.append(Objects.requireNonNull(title));
        this.body.append(Objects.requireNonNull(body));
    }
    public Html append(Object o) {
        body.append(o);
        return this;
    }
    public Html a(String link, Object text) { Html.a(link, text, body); return this; }
    public Html p(Object content) { Html.p(content, body); return this; }
    public Html h1(Object content) { Html.h1(content, body); return this; }
    public Html h3(Object content) { Html.h3(content, body); return this; }
    public Html table(Map data) { Html.table(data, body); return this; }
    public Html tag(String tagName, Object content) { Html.tag(tagName, content, body); return this; }
    public String getTitle() { return title.toString(); }
    public String getBody() { return body.toString(); }
    public String toString() {
        StringBuilder html = new StringBuilder(title.length() + body.length() + 120);
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        tag("title", title, html);
        html.append("</head>");
        tag("body", body, html);
        html.append("</html>");
        return html.toString();
    }
}
