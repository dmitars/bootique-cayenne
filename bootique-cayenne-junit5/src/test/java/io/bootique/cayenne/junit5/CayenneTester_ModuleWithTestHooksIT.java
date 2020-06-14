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
package io.bootique.cayenne.junit5;

import io.bootique.BQRuntime;
import io.bootique.di.DIRuntimeException;
import io.bootique.junit5.BQTestFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CayenneTester_ModuleWithTestHooksIT {

    @RegisterExtension
    static final BQTestFactory testFactory = new BQTestFactory();

    @Test
    @DisplayName("Tester should work with BQTestFactory-produced runtimes")
    public void testWithBQTestFactory() {
        CayenneTester tester = CayenneTester.create();
        BQRuntime runtime = testFactory.app().autoLoadModules().module(tester.moduleWithTestHooks()).createRuntime();
        ServerRuntime cayenneRuntime = runtime.getInstance(ServerRuntime.class);
        assertSame(cayenneRuntime, tester.getRuntime());
    }

    @Test
    @DisplayName("Reusing tester for multiple runtimes must be disallowed")
    public void testDisallowMultipleRuntimes() {
        CayenneTester tester = CayenneTester.create();
        testFactory.app().autoLoadModules().module(tester.moduleWithTestHooks()).createRuntime();

        assertThrows(DIRuntimeException.class, () ->
                testFactory.app().autoLoadModules().module(tester.moduleWithTestHooks()).createRuntime());
    }
}
