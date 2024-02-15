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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.jboss.as.quickstarts.cmt.model.LogMessage;

@ApplicationScoped
public class LogMessageManagerEJB {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Logs the customer name that a record is intended to be created for.
     *
     * @param name
     *     The name to log a record for.
     * @throws RollbackException
     *     When a log message for the passed-in name already exists.
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void logCreateCustomer(String name) throws RollbackException {
        LogMessage lm = new LogMessage();
        lm.setMessage("Attempt to create record for customer: '" + name + "'");

        entityManager.persist(lm);
    }

    /**
     * List all the log-messages.
     *
     * @return
     */
    @Transactional(TxType.NEVER)
    @SuppressWarnings("unchecked")
    public List<LogMessage> listLogMessages() {
        return entityManager.createQuery("select lm from LogMessage lm").getResultList();
    }
}
