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

package io.bootique.cayenne.test.junit5;

import io.bootique.cayenne.test.SchemaListener;
import io.bootique.test.junit5.BQTestFactory;
import org.apache.cayenne.map.DataMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CayenneTestJUnit5ModuleIT {

    @RegisterExtension
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testSchemaListeners() {

        SchemaListener listener = mock(SchemaListener.class);

        testFactory.app("-c", "classpath:config2.yml")
                .autoLoadModules()
                .module(b -> CayenneTestJUnit5Module.extend(b).addSchemaListener(listener))
                .createRuntime();

        verify(listener).afterSchemaCreated(any(DataMap.class));
    }
}
