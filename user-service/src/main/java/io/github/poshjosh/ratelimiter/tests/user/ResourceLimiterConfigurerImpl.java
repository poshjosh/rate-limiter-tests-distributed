package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.UsageListener;
import io.github.poshjosh.ratelimiter.util.LimiterConfig;
import io.github.poshjosh.ratelimiter.web.core.Registries;
import io.github.poshjosh.ratelimiter.web.core.ResourceLimiterConfigurer;

@javax.inject.Singleton
public class ResourceLimiterConfigurerImpl implements ResourceLimiterConfigurer {

    @Override
    public void configure(Registries registries) {
        //Beware of registering matchers for properties as they are universal
        //
        //registries.matchers().register(RateLimitPropertiesImpl.DEFAULT_CONFIG_NAME,
        //        containerRequestContext -> containerRequestContext.getUriInfo().getRequestUri().toString());
        registries.registerListener(new UsageListener() {
            @Override
            public void onConsumed(Object request, String resource, int permits, LimiterConfig<?> config) {
                System.out.println("ResourceLimitingConfigurerImpl " + resource + ", limit: " + config);
            }
        });
    }
}
