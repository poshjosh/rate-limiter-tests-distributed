package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.web.core.RateLimiterConfigurer;
import io.github.poshjosh.ratelimiter.web.core.registry.Registries;

@javax.inject.Singleton
public class RateLimiterConfigurerImpl implements RateLimiterConfigurer {

    @Override
    public void configure(Registries registries) {
        //Beware of registering matchers for properties as they are universal
        //
        //registries.matchers().register(RateLimitPropertiesImpl.DEFAULT_CONFIG_NAME,
        //        containerRequestContext -> containerRequestContext.getUriInfo().getRequestUri().toString());
    }
}
