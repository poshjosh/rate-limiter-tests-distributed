package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.util.Matcher;
import io.github.poshjosh.ratelimiter.web.core.RateLimiterConfigurer;
import io.github.poshjosh.ratelimiter.web.core.registry.Registry;

import javax.servlet.http.HttpServletRequest;

@javax.inject.Singleton
public class RateLimiterConfigurerImpl implements RateLimiterConfigurer {

    @Override
    public void configureMatchers(Registry<Matcher<HttpServletRequest>> registry) {
        //Beware of registering matchers for properties as they are universal
        //
        //registry.register(RateLimitPropertiesImpl.DEFAULT_CONFIG_NAME,
        //        containerRequestContext -> containerRequestContext.getUriInfo().getRequestUri().toString());
    }
}
