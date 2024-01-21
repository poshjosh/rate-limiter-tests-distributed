package io.github.poshjosh.ratelimiter.tests.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class HomeController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping("/error")
    public List<Message> error(HttpServletRequest request) {
        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final String sval = oval == null ? "Error encountered but no message" :
                oval.toString();
        if (log.isWarnEnabled()) {
            log.warn(sval);
        }
        return Trace.getAndClear();
    }

    @GetMapping
    public String home() {
        return "<h1>Message Server</h1>";
    }
}
