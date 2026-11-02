package com.projeto.seguranca;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.projeto.util.exception.LimiteDeTentativasException;

class ContadorTentativasLoginTest {

	@Test
	@DisplayName("A quinta falha bloqueia e a sexta tentativa é recusada")
	void cincoFalhasBloqueiam() {
		Clock clock = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
		ContadorTentativasLogin contador = new ContadorTentativasLogin(clock);

		for (int i = 0; i < 4; i++) {
			assertDoesNotThrow(() -> contador.registrarFalha("Admin@Quadrante.local"));
		}
		assertThrows(LimiteDeTentativasException.class, () -> contador.registrarFalha("admin@quadrante.local"));
		assertThrows(LimiteDeTentativasException.class, () -> contador.verificar("admin@quadrante.local"));
	}

	@Test
	@DisplayName("Sucesso zera o contador")
	void sucessoZera() {
		ContadorTentativasLogin contador = new ContadorTentativasLogin(Clock.systemUTC());
		contador.registrarFalha("a@a.com");
		contador.zerar("a@a.com");
		assertDoesNotThrow(() -> contador.verificar("a@a.com"));
	}

	@Test
	@DisplayName("Depois do prazo o bloqueio expira")
	void bloqueioExpira() {
		Relogio relogio = new Relogio(Instant.parse("2026-10-31T12:00:00Z"));
		ContadorTentativasLogin contador = new ContadorTentativasLogin(relogio);
		for (int i = 0; i < 4; i++) {
			contador.registrarFalha("a@a.com");
		}
		assertThrows(LimiteDeTentativasException.class, () -> contador.registrarFalha("a@a.com"));

		relogio.avancar(Duration.ofMinutes(6));
		assertDoesNotThrow(() -> contador.verificar("a@a.com"));
	}

	private static final class Relogio extends Clock {
		private Instant agora;

		private Relogio(Instant agora) {
			this.agora = agora;
		}

		private void avancar(Duration duracao) {
			agora = agora.plus(duracao);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return agora;
		}
	}
}
