package io.github.poshjosh.ratelimiter.tests.user;

import io.github.poshjosh.ratelimiter.annotations.Rate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.concurrent.TimeUnit;

@Path("/users")
public class UserResource {

    @Context
    private HttpServletRequest request;

    @GET
    @Produces("application/json")
    public String home() {
        return "User Service";
    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    @Rate(permits = 2, timeUnit = TimeUnit.MINUTES)
    public String getOne(@PathParam("id") String id) {
        return "User: " + id;
    }
}