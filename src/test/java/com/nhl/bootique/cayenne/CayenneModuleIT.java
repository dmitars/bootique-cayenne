package com.nhl.bootique.cayenne;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.jdbc.DataSourceFactory;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.shutdown.ShutdownManager;

public class CayenneModuleIT {

	private Module bqMocksModule;

	@Before
	public void before() {
		ConfigurationFactory mockConfigFactory = mock(ConfigurationFactory.class);
		BootLogger mockBootLogger = mock(BootLogger.class);
		ShutdownManager mockShutdownManager = mock(ShutdownManager.class);

		DataSourceFactory mockDataSourceFactory = mock(DataSourceFactory.class);
		when(mockDataSourceFactory.forName(anyString())).thenReturn(mock(DataSource.class));

		ServerRuntimeFactory serverRuntimeFactory = new ServerRuntimeFactory();
		serverRuntimeFactory.setDatasource("ds1");

		when(mockConfigFactory.config(ServerRuntimeFactory.class, "cayenne")).thenReturn(serverRuntimeFactory);

		this.bqMocksModule = b -> {
			b.bind(ConfigurationFactory.class).toInstance(mockConfigFactory);
			b.bind(DataSourceFactory.class).toInstance(mockDataSourceFactory);
			b.bind(BootLogger.class).toInstance(mockBootLogger);
			b.bind(ShutdownManager.class).toInstance(mockShutdownManager);
		};
	}

	@Test
	public void testDefaultConfig() {
		CayenneModule module = new CayenneModule();

		Injector i = Guice.createInjector(module, bqMocksModule);

		ServerRuntime runtime = i.getInstance(ServerRuntime.class);
		try {

			DataDomain domain = runtime.getDataDomain();
			assertNotNull(domain.getEntityResolver().getDbEntity("db_entity"));
		} finally {
			runtime.shutdown();
		}
	}

	@Test
	public void testNoConfig() {
		CayenneModule module = new CayenneModule().noConfig();

		Injector i = Guice.createInjector(module, bqMocksModule);

		ServerRuntime runtime = i.getInstance(ServerRuntime.class);
		try {

			DataDomain domain = runtime.getDataDomain();
			assertTrue(domain.getEntityResolver().getDbEntities().isEmpty());
		} finally {
			runtime.shutdown();
		}
	}
}
