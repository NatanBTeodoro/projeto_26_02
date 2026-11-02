package com.projeto.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.projeto.util.exception.LimiteDeTentativasException;

@Component
public class ContadorTentativasLogin {

	static final int LIMITE = 5;
	static final Duration BLOQUEIO = Duration.ofMinutes(5);

	private final Clock clock;
	private final ConcurrentHashMap<String, Estado> tentativas = new ConcurrentHashMap<>();

	@Autowired
	public ContadorTentativasLogin() {
		this(Clock.systemUTC());
	}

	ContadorTentativasLogin(Clock clock) {
		this.clock = clock;
	}

	public void verificar(String email) {
		Estado estado = tentativas.get(chave(email));
		if (estado == null || estado.bloqueadoAte == null) {
			return;
		}
		if (clock.instant().isBefore(estado.bloqueadoAte)) {
			throw new LimiteDeTentativasException("Muitas tentativas. Aguarde 5 minutos.");
		}
		tentativas.remove(chave(email));
	}

	public void registrarFalha(String email) {
		String chave = chave(email);
		Estado estado = tentativas.compute(chave, (ignorada, atual) -> {
			Instant agora = clock.instant();
			Estado proximo = atual;
			if (proximo == null || (proximo.bloqueadoAte != null && !agora.isBefore(proximo.bloqueadoAte))) {
				proximo = new Estado();
			}
			if (proximo.bloqueadoAte != null && agora.isBefore(proximo.bloqueadoAte)) {
				return proximo;
			}
			proximo.falhas++;
			if (proximo.falhas >= LIMITE) {
				proximo.bloqueadoAte = agora.plus(BLOQUEIO);
			}
			return proximo;
		});
		if (estado.falhas >= LIMITE && estado.bloqueadoAte != null && !clock.instant().isAfter(estado.bloqueadoAte)) {
			throw new LimiteDeTentativasException("Muitas tentativas. Aguarde 5 minutos.");
		}
	}

	public void zerar(String email) {
		tentativas.remove(chave(email));
	}

	private String chave(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private static final class Estado {
		private int falhas;
		private Instant bloqueadoAte;
	}
}
