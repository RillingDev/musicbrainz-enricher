package dev.rilling.musicbrainzenricher;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@SpringBootTest
@ContextConfiguration(initializers = {BaseIT.PostgresDataSourceInitializer.class})
@Testcontainers
abstract class BaseIT {

	@Container
	static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:12").withDatabaseName(
		"integration-tests-db").withUsername("sa").withPassword("sa");


	static class PostgresDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(Map.ofEntries(Map.entry("spring.datasource.url", POSTGRES_CONTAINER.getJdbcUrl()),
					Map.entry("spring.datasource.username", POSTGRES_CONTAINER.getUsername()),
					Map.entry("spring.datasource.password", POSTGRES_CONTAINER.getPassword())))
				.applyTo(applicationContext.getEnvironment());
		}
	}
}
