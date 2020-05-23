/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.cayenne.test;

import io.bootique.cayenne.test.tester.CayenneTesterBootiqueHook;
import io.bootique.cayenne.test.tester.CayenneRuntimeManager;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A JUnit5 extension that manages test schema, data and Cayenne runtime state between the tests.
 *
 * @since 2.0
 */
public class CayenneTester implements BeforeEachCallback {

    private boolean refreshCayenneCaches;
    private boolean deleteBeforeEachTest;
    private Collection<Class<? extends Persistent>> entities;

    private CayenneTesterBootiqueHook bootiqueHook;
    private CayenneRuntimeManager runtimeManager;

    public static CayenneTester create() {
        return new CayenneTester();
    }

    protected CayenneTester() {

        this.bootiqueHook = new CayenneTesterBootiqueHook()
                // lazy init helps to prevent certain unneeded steps, such as DB cleanup before the first test
                // TODO: are we shooting ourselves in the foot with this? Is it reasonable to expect a dirty
                //   DB before the first test?
                .onFirstAccess(r -> resolveRuntimeManager(r))
                .onFirstAccess(r -> getRuntimeManager().createSchema());

        this.refreshCayenneCaches = true;
        this.deleteBeforeEachTest = false;
        this.entities = new ArrayList<>();
    }

    public CayenneTester doNoRefreshCayenneCaches() {
        this.refreshCayenneCaches = false;
        return this;
    }

    /**
     * Configures the Tester to manage a subset of entities out a potentially very large number of entities in the model.
     *
     * @param entities a list of entities to manage (create schema for, delete test data, etc.)
     * @return this tester
     */
    public CayenneTester entities(Class<? extends Persistent>... entities) {
        for (Class<? extends Persistent> e : entities) {
            this.entities.add(e);
        }

        return this;
    }

    /**
     * Configures the Tester to delete data corresponding to the tester's entity model before each test.
     *
     * @return this tester
     */
    public CayenneTester deleteBeforeEachTest() {
        this.deleteBeforeEachTest = true;
        return this;
    }

    /**
     * Returns a a new Bootique module that registers Cayenne test extensions that allow the tester to interact
     * with Bootique runtime defined somewhere in the test.
     *
     * @return a new Bootique module that registers Cayenne test extensions
     */
    public BQModule registerTestHooks() {
        return this::configure;
    }

    protected void configure(Binder binder) {
        binder.bind(CayenneTesterBootiqueHook.class).toInstance(bootiqueHook)
                // using "initOnStartup" to cause immediate Cayenne initialization. Any downsides?
                .initOnStartup();
    }

    public ServerRuntime getRuntime() {
        return bootiqueHook.getRuntime();
    }

    protected CayenneRuntimeManager getRuntimeManager() {
        Assertions.assertNotNull(runtimeManager, "Cayenne runtime is not resolved. Called outside of test lifecycle?");
        return runtimeManager;
    }

    public String getTableName(Class<? extends Persistent> entity) {
        ObjEntity e = getRuntime().getDataDomain().getEntityResolver().getObjEntity(entity);
        if (e == null) {
            throw new IllegalStateException("Type is not mapped in Cayenne: " + entity);
        }

        return e.getDbEntity().getName();
    }

    protected void resolveRuntimeManager(ServerRuntime runtime) {
        this.runtimeManager = CayenneRuntimeManager
                .builder(runtime.getDataDomain())
                .entities(entities)
                .build();
    }

    @Override
    public void beforeEach(ExtensionContext context) {

        if (bootiqueHook.isInitialized()) {
            if (refreshCayenneCaches) {
                getRuntimeManager().refreshCaches();
            }

            if (deleteBeforeEachTest && !entities.isEmpty()) {
                getRuntimeManager().deleteData();
            }
        }
    }
}
