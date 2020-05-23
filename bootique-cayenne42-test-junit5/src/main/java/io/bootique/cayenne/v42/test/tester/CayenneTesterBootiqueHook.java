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
package io.bootique.cayenne.v42.test.tester;

import org.apache.cayenne.configuration.server.ServerRuntime;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CayenneTesterBootiqueHook {

    @Inject
    ServerRuntime runtime;

    private Collection<Consumer<ServerRuntime>> onInitCallbacks;
    private boolean callbacksPending;

    public CayenneTesterBootiqueHook() {
        this.onInitCallbacks = new ArrayList<>();
        this.callbacksPending = true;
    }

    public boolean isInitialized() {
        return !callbacksPending;
    }

    public CayenneTesterBootiqueHook onFirstAccess(Consumer<ServerRuntime> callback) {

        if (!callbacksPending) {
            throw new IllegalStateException("Callbacks already processed");
        }

        onInitCallbacks.add(callback);
        return this;
    }

    public ServerRuntime getRuntime() {
        assertNotNull(runtime, "ServerRuntime is not initialized. Not connected to Bootique runtime?");

        if (callbacksPending) {
            synchronized (this) {
                if (callbacksPending) {
                    onInitCallbacks.forEach(c -> c.accept(runtime));
                    this.callbacksPending = false;
                }
            }
        }

        return runtime;
    }
}
