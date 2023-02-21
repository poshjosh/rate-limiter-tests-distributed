package io.github.poshjosh.ratelimiter.tests.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Log all application properties.
 *
 * <pre>
 * <code>
 * @SpringBootApplication
 * public class MyApp {
 *     public static void main(String[] args) {
 *         SpringApplication springApplication = new SpringApplication(MyApp.class);
 *         springApplication.addListeners(new PropertiesLogger());
 *         springApplication.run(args);
 *     }
 * }
 * </code>
 * </pre>
 */
public class PropertiesLogger implements ApplicationListener<ApplicationPreparedEvent> {
    private static final Logger log = LoggerFactory.getLogger(PropertiesLogger.class);

    private ConfigurableEnvironment environment;
    private boolean isFirstRun = true;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        if (isFirstRun) {
            environment = event.getApplicationContext().getEnvironment();
            printProperties();
        }
        isFirstRun = false;
    }

    public void printProperties() {
        StringBuilder props = new StringBuilder();
        props.append("\n\nPRINTING APPLICATION PROPERTIES");
        for (EnumerablePropertySource propertySource : findPropertiesPropertySources()) {
            props.append("\n\n******* " + propertySource.getName() + " *******");
            String[] propertyNames = propertySource.getPropertyNames();
            Arrays.sort(propertyNames);
            for (String propertyName : propertyNames) {
                String resolvedProperty = environment.getProperty(propertyName);
                String sourceProperty = propertySource.getProperty(propertyName).toString();
                props.append('\n').append(propertyName).append('=');
                if(resolvedProperty.equals(sourceProperty)) {
                    props.append(resolvedProperty);
                }else {
                    props.append(sourceProperty).append(" overriden to ").append(resolvedProperty);
                }
            }
        }
        log.info("{}", props);
    }

    private List<EnumerablePropertySource> findPropertiesPropertySources() {
        List<EnumerablePropertySource> propertiesPropertySources = new LinkedList<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                propertiesPropertySources.add((EnumerablePropertySource) propertySource);
            }
        }
        return propertiesPropertySources;
    }
}