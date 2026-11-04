package com.projeto.util;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.projeto.util.exception.NaoAutenticadoException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;

@RestControllerAdvice
public class TratadorExcecoes {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<Map<String, String>> naoEncontrado(RecursoNaoEncontradoException ex) {
		return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, String>> rotaInexistente(NoResourceFoundException ex) {
		return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado");
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<Map<String, String>> regra(RegraDeNegocioException ex) {
		return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException ex) {
		String mensagem = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getDefaultMessage())
				.filter(texto -> texto != null && !texto.isBlank())
				.findFirst()
				.orElse("Dados inválidos");
		return resposta(HttpStatus.BAD_REQUEST, mensagem);
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<Map<String, String>> malformada(Exception ex) {
		return resposta(HttpStatus.BAD_REQUEST, "Requisição malformada");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> integridade(DataIntegrityViolationException ex) {
		return resposta(HttpStatus.BAD_REQUEST, "Dados já cadastrados");
	}

	@ExceptionHandler(NaoAutenticadoException.class)
	public ResponseEntity<Map<String, String>> naoAutenticado(NaoAutenticadoException ex) {
		return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, String>> autenticacao(AuthenticationException ex) {
		return resposta(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> proibido(AccessDeniedException ex) {
		return resposta(HttpStatus.FORBIDDEN, "Acesso negado");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> inesperado(Exception ex) {
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");
	}

	private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
	}
}
