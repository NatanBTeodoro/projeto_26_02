package com.projeto.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RenderDatabaseUrlTest {

	@Test
	@DisplayName("A URL interna do Render fica sem SSL")
	void urlInternaSemSsl() {
		Map<String, Object> propriedades = RenderDatabaseUrl.traduzir(
				"postgresql://quadrante:senha@dpg-abc-a/menu_engineering");

		assertEquals("jdbc:postgresql://dpg-abc-a:5432/menu_engineering", propriedades.get("spring.datasource.url"));
	}

	@Test
	@DisplayName("Converte a URL do Render para JDBC com SSL")
	void converte() {
		Map<String, Object> propriedades = RenderDatabaseUrl.traduzir(
				"postgresql://quadrante:segredo%401@dpg-abc.oregon-postgres.render.com:5432/menu_engineering");

		assertEquals(
				"jdbc:postgresql://dpg-abc.oregon-postgres.render.com:5432/menu_engineering?sslmode=require",
				propriedades.get("spring.datasource.url"));
		assertEquals("quadrante", propriedades.get("spring.datasource.username"));
		assertEquals("segredo@1", propriedades.get("spring.datasource.password"));
	}

	@Test
	@DisplayName("URL JDBC e valor vazio ficam como estão")
	void ignoraJdbcEVazio() {
		assertTrue(RenderDatabaseUrl.traduzir(null).isEmpty());
		assertTrue(RenderDatabaseUrl.traduzir("  ").isEmpty());
		assertTrue(RenderDatabaseUrl.traduzir("jdbc:postgresql://localhost:5432/menu_engineering").isEmpty());
	}

	@Test
	@DisplayName("URL sem credenciais interrompe a subida")
	void semCredenciais() {
		assertThrows(IllegalStateException.class,
				() -> RenderDatabaseUrl.traduzir("postgresql://localhost:5432/menu_engineering"));
	}
}
