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
package org.jboss.as.quickstarts.cmt.ejb;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.as.quickstarts.cmt.model.Customer;

@ApplicationScoped
public class CustomerManagerEJB {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private LogMessageManagerEJB logMessageManager;

    @Inject
    private InvoiceManagerEJB invoiceManager;

    @Transactional(TxType.REQUIRED)
    public void createCustomer(String name) throws RollbackException, EntityExistsException {
        try {
            logMessageManager.logCreateCustomer(name);
        } catch (RollbackException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new EntityExistsException(e.getCause());
            } else {
                throw e;
            }
        }

        Customer c1 = new Customer();
        c1.setName(name);
        entityManager.persist(c1);

        invoiceManager.createInvoice(name);

        // It could be done before all the 'storing' but this is just to show that
        // the invoice is not delivered when we cause an EJBException
        // after the fact but before the transaction is committed.
        if (!nameIsValid(name)) {
            throw new IllegalArgumentException("Invalid name: customer names should only contain letters & '-'");
        }
    }

    static boolean nameIsValid(String name) {
        return name.matches("[\\p{L}-]+");
    }

    /**
     * List all the customers.
     *
     * @return
     */
    @Transactional(TxType.NEVER)
    @SuppressWarnings("unchecked")
    public List<Customer> listCustomers() {
        return entityManager.createQuery("select c from Customer c").getResultList();
    }
}
