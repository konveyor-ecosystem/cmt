package org.jboss.as.quickstarts.cmt.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Path("/")
public class ContextRoot {

    /**
     * Performs a redirect on the base context root, to the endpoint for rendering the "addCustomer" template.
     *
     * @return
     *     A Response that redirects to user to "/customers/addCustomer"
     */
    @GET
    public Response redirectToAddCustomer() {
        UriBuilder uriBuilder = UriBuilder.fromPath("/customers/addCustomer");

        return Response
                .seeOther(uriBuilder.build())
                .build();
    }
}
