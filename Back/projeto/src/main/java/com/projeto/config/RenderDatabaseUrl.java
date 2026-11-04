package com.projeto.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * O Render entrega {@code DATABASE_URL} no formato {@code postgresql://}.
 * O JDBC do PostgreSQL só aceita {@code jdbc:postgresql://}.
 */
public class RenderDatabaseUrl implements EnvironmentPostProcessor, Ordered {

	public RenderDatabaseUrl() {
	}

	public RenderDatabaseUrl(org.springframework.boot.logging.DeferredLogFactory ignorado) {
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> propriedades = traduzir(environment.getProperty("DATABASE_URL"));
		if (propriedades.isEmpty()) {
			return;
		}
		environment.getPropertySources().addFirst(new MapPropertySource("render-database", propriedades));
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	static Map<String, Object> traduzir(String databaseUrl) {
		if (databaseUrl == null || databaseUrl.isBlank() || databaseUrl.startsWith("jdbc:")) {
			return Map.of();
		}
		String normalizada = databaseUrl.startsWith("postgres://")
				? "postgresql://" + databaseUrl.substring("postgres://".length())
				: databaseUrl;
		URI uri = URI.create(normalizada);
		String userInfo = uri.getRawUserInfo();
		if (userInfo == null || !userInfo.contains(":")) {
			throw new IllegalStateException("DATABASE_URL sem usuário e senha");
		}
		int separador = userInfo.indexOf(':');
		String usuario = decodificar(userInfo.substring(0, separador));
		String senha = decodificar(userInfo.substring(separador + 1));
		int porta = uri.getPort() > 0 ? uri.getPort() : 5432;
		String host = uri.getHost();
		String caminho = uri.getPath() == null ? "" : uri.getPath();
		String consulta = uri.getRawQuery();
		boolean externo = host != null && host.contains(".");
		String parametros = consulta;
		if (externo && (consulta == null || !consulta.contains("sslmode="))) {
			parametros = consulta == null || consulta.isBlank() ? "sslmode=require" : consulta + "&sslmode=require";
		}
		String url = "jdbc:postgresql://" + host + ":" + porta + caminho
				+ (parametros == null || parametros.isBlank() ? "" : "?" + parametros);

		Map<String, Object> propriedades = new LinkedHashMap<>();
		propriedades.put("spring.datasource.url", url);
		propriedades.put("spring.datasource.username", usuario);
		propriedades.put("spring.datasource.password", senha);
		return propriedades;
	}

	private static String decodificar(String valor) {
		return URLDecoder.decode(valor, StandardCharsets.UTF_8);
	}
}
