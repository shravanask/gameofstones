package com.shravan.gameofstones.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("play")
public class UserResource {

    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }
}
