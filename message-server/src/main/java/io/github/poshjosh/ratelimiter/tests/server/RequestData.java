package io.github.poshjosh.ratelimiter.tests.server;

import javax.servlet.http.HttpServletRequest;

final class RequestData {
    private final String sessionId;
    private final String referrer;
    private final String method;
    private final String uri;

    RequestData(HttpServletRequest request) {
        this.sessionId = request.getSession().getId();
        this.referrer = request.getHeader("referer");
        this.method = request.getMethod();
        this.uri = request.getRequestURI();
    }
    @Override
    public String toString() {
        return "RequestData{" + "sessionId='" + sessionId + '\'' + ", referrer='" + referrer + '\''
                + ", method='" + method + '\'' + ", uri='" + uri + '\'' + '}';
    }
}
