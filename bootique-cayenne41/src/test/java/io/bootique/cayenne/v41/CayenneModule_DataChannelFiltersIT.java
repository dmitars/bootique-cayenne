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
package io.bootique.cayenne.v41;

import io.bootique.di.BQModule;
import io.bootique.test.junit5.BQTestFactory;
import org.apache.cayenne.*;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.graph.GraphDiff;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// DataChannelFilter is deprecated in Cayenne
@Deprecated
public class CayenneModule_DataChannelFiltersIT {

    @RegisterExtension
    public BQTestFactory testFactory = new BQTestFactory();

    private ServerRuntime runtimeWithFilters(DataChannelFilter... filters) {

        BQModule filtersModule = (binder) -> {

            CayenneModuleExtender extender = CayenneModule.extend(binder);
            Arrays.asList(filters).forEach(extender::addFilter);
        };

        return testFactory.app("--config=classpath:genericconfig.yml")
                .autoLoadModules()
                .module(filtersModule)
                .createRuntime()
                .getInstance(ServerRuntime.class);
    }

    @Test
    public void testFilters() {

        DataChannelFilter f = mock(DataChannelFilter.class);
        when(f.onSync(any(), any(), anyInt(), (DataChannelSyncFilterChain) any())).thenReturn(mock(GraphDiff.class));

        CayenneDataObject o1 = new CayenneDataObject();
        o1.setObjectId(new ObjectId("T1"));
        o1.writeProperty("name", "n" + 1);

        CayenneDataObject o2 = new CayenneDataObject();
        o2.setObjectId(new ObjectId("T1"));
        o2.writeProperty("name", "n" + 2);

        ServerRuntime runtime = runtimeWithFilters(f);
        try {
            ObjectContext c = runtime.newContext();
            c.registerNewObject(o1);
            c.registerNewObject(o2);
            c.commitChanges();

        } finally {
            runtime.shutdown();
        }

        verify(f).onSync(any(), any(), anyInt(), (DataChannelSyncFilterChain) any());
    }
}

