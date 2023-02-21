package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.util.Rate;
import io.github.poshjosh.ratelimiter.util.Rates;
import io.github.poshjosh.ratelimiter.web.core.util.RateLimitProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@javax.inject.Singleton
public class RateLimitPropertiesImpl implements RateLimitProperties {

    public static final String DEFAULT_CONFIG_NAME = "default";

    private final String applicationPath;

    private final List<String> resourcePackages;

    private final Boolean disabled;

    private final Map<String, Rates> rateLimitConfigs;

    public RateLimitPropertiesImpl() {
        this.applicationPath = UserService.APPLICATION_PATH;
        this.resourcePackages = Collections.singletonList(UserResource.class.getPackage().getName());
        this.disabled = Boolean.FALSE;
        this.rateLimitConfigs = Collections.singletonMap(DEFAULT_CONFIG_NAME, getRateLimitConfigList());
    }

    @Override public String getApplicationPath() {
        return applicationPath;
    }

    @Override public List<Class<?>> getResourceClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getResourcePackages() {
        return resourcePackages;
    }

    @Override
    public Boolean getDisabled() {
        return disabled;
    }

    @Override
    public Map<String, Rates> getRateLimitConfigs() {
        return rateLimitConfigs;
    }

    private Rates getRateLimitConfigList() {
        return Rates.of(getRateLimits());
    }

    private Rate[] getRateLimits() {
        return new Rate[]{Rate.of(2, Duration.ofMinutes(1))};
    }

    @Override
    public String toString() {
        return "RateLimitPropertiesImpl{" +
                "applicationPath=" + applicationPath +
                ", resourceClasses=" + getResourceClasses() +
                ", resourcePackages=" + resourcePackages +
                ", disabled=" + disabled +
                ", rateLimitConfigs=" + rateLimitConfigs +
                '}';
    }
}
