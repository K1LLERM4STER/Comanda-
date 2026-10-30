package com.comanda.comanda;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

@Testcontainers
@SpringBootTest(properties = {
		"spring.config.import=",
		"spring.flyway.enabled=true",
		"spring.flyway.locations=classpath:db/migration",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class ComandaApplicationTests {

	@Container
	// A extensao JUnit do Testcontainers inicia e encerra este recurso.
	@SuppressWarnings("resource")
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
			.withDatabaseName("comanda_test");

	@DynamicPropertySource
	static void configurarBancoTemporario(DynamicPropertyRegistry registry) {
		// Nunca usar as credenciais ou a URL do PostgreSQL local nos testes.
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.flyway.url", postgres::getJdbcUrl);
		registry.add("spring.flyway.user", postgres::getUsername);
		registry.add("spring.flyway.password", postgres::getPassword);
	}

	@Autowired
	DataSource dataSource;

	@Autowired
	Flyway flyway;

	@Autowired
	EntityManagerFactory entityManagerFactory;

	@Test
	void contextLoads() throws Exception {
		try (var connection = dataSource.getConnection()) {
			assertThat(connection.getMetaData().getURL()).isEqualTo(postgres.getJdbcUrl());
			assertThat(connection.getCatalog()).isEqualTo("comanda_test");
		}

		assertThat(Arrays.stream(flyway.info().applied())
				.anyMatch(migration -> migration.getVersion() != null
						&& "1".equals(migration.getVersion().getVersion())
						&& migration.getState().isApplied())).isTrue();
		assertThat(flyway.info().pending()).isEmpty();
		assertThat(entityManagerFactory.isOpen()).isTrue();
		assertThat(entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto"))
				.isEqualTo("validate");
	}

}
