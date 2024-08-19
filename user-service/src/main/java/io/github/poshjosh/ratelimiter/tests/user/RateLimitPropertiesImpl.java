package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.model.Rate;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.util.RateLimitProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@javax.inject.Singleton
public class RateLimitPropertiesImpl implements RateLimitProperties {

    public static final String DEFAULT_CONFIG_NAME = "default";

    private final String applicationPath;

    private final List<String> resourcePackages;

    private final Boolean disabled;

    private final List<Rates> rates;

    public RateLimitPropertiesImpl() {
        this.applicationPath = UserService.APPLICATION_PATH;
        this.resourcePackages = Collections.singletonList(UserResource.class.getPackage().getName());
        this.disabled = Boolean.FALSE;
        this.rates = Collections.singletonList(createRates());
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
    public List<Rates> getRates() {
        return rates;
    }

    private Rates createRates() {
        return Rates.of(DEFAULT_CONFIG_NAME, createRate());
    }

    private Rate createRate() {
        return Rate.of(2, Duration.ofMinutes(1));
    }

    @Override
    public String toString() {
        return "RateLimitPropertiesImpl{" +
                "applicationPath=" + applicationPath +
                ", resourceClasses=" + getResourceClasses() +
                ", resourcePackages=" + resourcePackages +
                ", disabled=" + disabled +
                ", rates=" + rates +
                '}';
    }
}
