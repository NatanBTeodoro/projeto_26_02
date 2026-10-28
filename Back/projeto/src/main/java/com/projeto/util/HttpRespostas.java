package com.projeto.util;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Monta {@link ResponseEntity} com os status HTTP usados na API.
 * Sucesso (2xx) e erros (4xx/5xx) ficam no mesmo lugar para os controllers
 * e o {@link com.projeto.util.TratadorExcecoes} não espalharem códigos soltos.
 */
public final class HttpRespostas {

	private HttpRespostas() {
	}

	public static <T> ResponseEntity<T> ok(T corpo) {
		return ResponseEntity.ok(corpo);
	}

	public static <T> ResponseEntity<T> created(URI localizacao, T corpo) {
		return ResponseEntity.created(localizacao).body(corpo);
	}

	public static ResponseEntity<Void> noContent() {
		return ResponseEntity.noContent().build();
	}

	public static ResponseEntity<ErroResposta> badRequest(ErroResposta erro) {
		return ResponseEntity.badRequest().body(erro);
	}

	public static ResponseEntity<ErroResposta> unauthorized(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
	}

	public static ResponseEntity<ErroResposta> forbidden(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
	}

	public static ResponseEntity<ErroResposta> notFound(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
	}

	public static ResponseEntity<ErroResposta> conflict(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
	}

	public static ResponseEntity<ErroResposta> unprocessable(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
	}

	public static ResponseEntity<ErroResposta> tooManyRequests(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(erro);
	}

	public static ResponseEntity<ErroResposta> methodNotAllowed(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(erro);
	}

	public static ResponseEntity<ErroResposta> payloadTooLarge(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(erro);
	}

	public static ResponseEntity<ErroResposta> internalError(ErroResposta erro) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
	}
}
