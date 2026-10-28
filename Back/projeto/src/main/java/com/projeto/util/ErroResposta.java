package com.projeto.util;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErroResposta(
		Instant timestamp,
		int status,
		String erro,
		String mensagem,
		String caminho,
		List<String> detalhes) {

	public static ErroResposta of(HttpStatus status, String mensagem, String caminho) {
		return of(status, mensagem, caminho, List.of());
	}

	public static ErroResposta of(HttpStatus status, String mensagem, String caminho, List<String> detalhes) {
		return new ErroResposta(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				mensagem,
				caminho,
				detalhes == null ? List.of() : List.copyOf(detalhes));
	}
}
