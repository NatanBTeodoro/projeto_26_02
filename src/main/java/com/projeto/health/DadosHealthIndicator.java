package com.projeto.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DadosHealthIndicator implements HealthIndicator {

	private final JdbcTemplate jdbcTemplate;

	public DadosHealthIndicator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Health health() {
		try {
			Integer um = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			if (um != null && um == 1) {
				return Health.up().build();
			}
			return Health.down().build();
		} catch (RuntimeException ex) {
			return Health.down().build();
		}
	}
}
