package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.web.core.ResourceLimiterConfigurer;
import io.github.poshjosh.ratelimiter.web.core.util.RateLimitProperties;
import io.github.poshjosh.ratelimiter.web.javaee.ResourceLimitingDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

@javax.ws.rs.ext.Provider
public class ResourceLimitingDynamicFeatureImpl extends ResourceLimitingDynamicFeature {

    private static final Logger log = LoggerFactory.getLogger(ResourceLimitingDynamicFeatureImpl.class);

    @javax.inject.Inject
    public ResourceLimitingDynamicFeatureImpl(
            RateLimitProperties properties, ResourceLimiterConfigurer configurer) {
        super(properties, configurer);
        log.info("Properties: {}", properties);
    }

    @Override
    protected void onLimitExceeded(HttpServletRequest request, ContainerRequestContext context) {
        System.out.println("onLimitExceeded Request: " + request);
        // throws this funny exception
        // java.lang.NoSuchMethodError: javax.ws.rs.core.Response.status(ILjava/lang/String;)Ljavax/ws/rs/core/Response$ResponseBuilder;
        //requestContext.abortWith(
        //        Response.status(429, "Too many requests").build());
        throw new WebApplicationException("Too many requests", 429);
    }
}
