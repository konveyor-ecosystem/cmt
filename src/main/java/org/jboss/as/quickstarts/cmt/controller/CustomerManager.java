/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.cmt.controller;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.as.quickstarts.cmt.ejb.CustomerManagerEJB;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/customers")
public class CustomerManager {

    // Template for listing customers
    @Inject
    Template customers;

    // Template for adding customers
    @Inject
    Template addCustomer;

    // Template for "Invalid Name" error when adding customers
    @Inject
    Template invalidName;

    // Template for "Duplicate Customer" error when adding customers
    @Inject
    Template duplicate;

    @Inject
    private CustomerManagerEJB customerManager;

    /**
     * Renders a page to display the list of all customers.
     *
     * @return
     *     The page rendered from the "customers" Qute template.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response renderCustomersPage() {
        TemplateInstance customersInstance = customers
                .data("customers", customerManager.listCustomers());

        return Response
                .ok(customersInstance.render())
                .build();
    }

    /**
     * Renders a page allowing the user to add a new customer.
     *
     * @return
     *     The page rendered from the "addCustomer" Qute template
     */
    @GET
    @Path("/addCustomer")
    @Produces(MediaType.TEXT_HTML)
    public Response renderAddCustomersPage() {
        return Response
                .ok(addCustomer.render())
                .build();
    }

    /**
     * Renders a page to display the result of the call to POST /customers/addCustomer.
     *
     * @param name
     *     The name of the customer to add.
     * @return
     *     The page rendered from the appropriate Qute template. This will be "customers" on success, but can be 
     * one of many based on the error: "invalidName" for name validation failures:
     * <p>    - "invalidName": When name validation fails.
     * <p>    - "duplicate": When the name submitted already exists.
     * <p>    - "addCustomer": For all other errors, return to the "addCustomer" page, but add an error message.
     */
    @POST
    @Path("/addCustomer")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addCustomer(@FormParam("name") String name) {
        try {
            customerManager.createCustomer(name);

            TemplateInstance customersInstance = customers
                    .data("customers", customerManager.listCustomers());

            return Response
                    .ok(customersInstance.render())
                    .build();
        } catch (IllegalArgumentException e) {
            return buildInvalidNameResponse(e, name);
        } catch (EntityExistsException e) {
            return buildDuplicateNameResponse(e, name);
        } catch (Exception e) {
            return buildUnexpectedExceptionResponse(e, name);
        }
    }


    /**
     * Renders a separate page from "addCustomer" for indicating the name already exists and cannot be added.
     *
     * @param e
     *     The exception indicating the entered name does not pass validation. Use "Exception" to allow multiple types.
     * @param name
     *     The name that failed to add.
     * @return
     *     The page rendered from the "invalidName" template.
     */
    private Response buildInvalidNameResponse(Exception e, String name) {
        Log.warn("Invalid name: " + e.getLocalizedMessage(), e);

        TemplateInstance invalidNameInstance = invalidName
                .data("name", name);

        return Response
                // Unprocessable Content
                .status(422)
                .entity(invalidNameInstance.render())
                .build();
    }

    /**
     * Renders a separate page from "addCustomer" for indicating the name already exists and cannot be added.
     *
     * @param e
     *     The exception indicating the entity was a duplicate. Use "Exception" to allow multiple types.
     * @param name
     *     The name that failed to add.
     * @return
     *     The page rendered from the "duplicate" template.
     */
    private Response buildDuplicateNameResponse(Exception e, String name) {
        Log.warn("Caught a duplicate: " + e.getLocalizedMessage(), e);

        TemplateInstance duplicateInstance = duplicate
                .data("name", name);

        return Response
                .status(Status.CONFLICT)
                .entity(duplicateInstance.render())
                .build();
    }

    /**
     * Returns the user to the page for adding a new customer, with a message containing the Exception message (but not
     * the stack trace).
     *
     * @param e
     *     The "catch-all" exception, used to prevent an Exception from being sent back to the client.
     * @param name
     *     The name that failed to add.
     * @return
     *     The page rendered from the "addCustomer" Qute template.
     */
    private Response buildUnexpectedExceptionResponse(Exception e, String name) {
        String error = "Unexpected exception: " + e.getLocalizedMessage();

        Log.error(error, e);

        TemplateInstance addCustomerInstance = addCustomer
                .data("messages", List.of(error))
                .data("name", name);

        return Response
                .serverError()
                .entity(addCustomerInstance.render())
                .build();
    }
}
