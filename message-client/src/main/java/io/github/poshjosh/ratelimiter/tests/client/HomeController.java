package io.github.poshjosh.ratelimiter.tests.client;

import io.github.poshjosh.ratelimiter.tests.client.performance.PerformanceTestData;
import io.github.poshjosh.ratelimiter.tests.client.performance.RequestSpreadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class HomeController implements ErrorController {

    private final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final Rest rest;
    private final UsageService usageService;
    public HomeController(Rest rest, UsageService usageService) {
        this.rest = rest;
        this.usageService = usageService;
    }

    @GetMapping("/forward")
    public ResponseEntity<Object> forwardToServer(@RequestParam("to") String to) {
        return rest.getFromServer(to, Object.class, e -> e.toString());
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request) {
        log.debug("#error()");
        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final String sval = oval == null ? "" : oval.toString();
        return Html.of("Error", sval.isEmpty() ? "An unexpected error occurred" : sval).toString();
    }

    @GetMapping
    public String home() {
        log.debug("#home()");
        final String name = "Tests";
        PerformanceTestData defaultValues = new PerformanceTestData();
        return Html.of(name)
                .h1(name)
                .h3("For consistent results, tests should be run once per deployment")
                .a(ResourcePaths.TESTS_PATH, "Click here to run tests")
                .p("Fill and submit the form below to run performance tests")
                .append("<form method=\"post\" action=\"")
                .append(ResourcePaths.PERFORMANCE_TESTS_PATH)
                .append("\">")
                .p(numberInput("limit", defaultValues.getLimit()))
                .p(numberInput("timeout", defaultValues.getTimeout()))
                .p(numberInput("work", defaultValues.getWork()))
                .p(numberInput("percent", defaultValues.getPercent()))
                .p(numberInput("iterations", defaultValues.getIterations()))
                .p(numberInput("durationPerTestUser", defaultValues.getDurationPerTestUser()))
                .p(enumInput("requestSpreadType", RequestSpreadType.class))
                .p("<input type=\"submit\" value=\"Submit\"/>")
                .append("</form>")
                .h3("Stats")
                .table(usageService.stats().getBody())
                .toString();
    }

    private String numberInput(String name, int value) {
        Objects.requireNonNull(name);
        return Html.tag("label", "for", name, name)
                .append("<br/><input type=\"number\" min=\"0\" max=\"500\" name=\"")
                .append(name).append("\" value=\"").append(value).append("\"/>").toString();
    }

    private <T extends Enum> String enumInput(String name, Class<T> enumType) {
        Objects.requireNonNull(name);
        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("Expected enum type, found: " + enumType);
        }
        T [] enumConstants = enumType.getEnumConstants();

        if (enumConstants == null || enumConstants.length == 0) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        for(T enumConstant : enumConstants) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("value", enumConstant.name());
            attributes.put("title", enumConstant.toString());
            Html.tag("option", attributes, enumConstant.name(), buff);
        }
        final String options = buff.toString();

        buff.setLength(0);
        Html.tag("label", "for", name, name, buff).append("<br/>");
        return Html.tag("select", "name", name, options, buff).toString();
    }
}