package com.projeto.util;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.projeto.util.exception.LimiteDeTentativasException;
import com.projeto.util.exception.NaoAutenticadoException;
import com.projeto.util.exception.OperacaoNaoPermitidaException;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;
import com.projeto.util.exception.RequisicaoInvalidaException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class TratadorExcecoes {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
		return HttpRespostas.notFound(ErroResposta.of(HttpStatus.NOT_FOUND, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErroResposta> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
		return HttpRespostas.notFound(ErroResposta.of(HttpStatus.NOT_FOUND, "Recurso não encontrado", path(req)));
	}

	@ExceptionHandler(RequisicaoInvalidaException.class)
	public ResponseEntity<ErroResposta> requisicaoInvalida(RequisicaoInvalidaException ex, HttpServletRequest req) {
		return HttpRespostas.badRequest(ErroResposta.of(HttpStatus.BAD_REQUEST, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResposta> beanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
				.map(this::campo)
				.toList();
		return HttpRespostas.badRequest(
				ErroResposta.of(HttpStatus.BAD_REQUEST, "Dados inválidos", path(req), detalhes));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErroResposta> constraint(ConstraintViolationException ex, HttpServletRequest req) {
		List<String> detalhes = ex.getConstraintViolations().stream()
				.map(v -> v.getPropertyPath() + ": " + v.getMessage())
				.toList();
		return HttpRespostas.badRequest(
				ErroResposta.of(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", path(req), detalhes));
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ErroResposta> corpoOuParametroInvalido(Exception ex, HttpServletRequest req) {
		return HttpRespostas.badRequest(
				ErroResposta.of(HttpStatus.BAD_REQUEST, "Requisição malformada", path(req)));
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ErroResposta> ordenacaoInvalida(PropertyReferenceException ex, HttpServletRequest req) {
		String mensagem = "Não é possível ordenar pelo campo '" + ex.getPropertyName() + "'";
		return HttpRespostas.badRequest(ErroResposta.of(HttpStatus.BAD_REQUEST, mensagem, path(req)));
	}

	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<ErroResposta> usoInvalido(InvalidDataAccessApiUsageException ex, HttpServletRequest req) {
		if (ex.getCause() instanceof PropertyReferenceException propriedade) {
			return ordenacaoInvalida(propriedade, req);
		}
		return HttpRespostas.badRequest(
				ErroResposta.of(HttpStatus.BAD_REQUEST, "Requisição malformada", path(req)));
	}

	@ExceptionHandler(RecursoJaExistenteException.class)
	public ResponseEntity<ErroResposta> conflito(RecursoJaExistenteException ex, HttpServletRequest req) {
		return HttpRespostas.conflict(ErroResposta.of(HttpStatus.CONFLICT, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
		return HttpRespostas.conflict(
				ErroResposta.of(HttpStatus.CONFLICT, "Registro em conflito com uma restrição do banco", path(req)));
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<ErroResposta> regra(RegraDeNegocioException ex, HttpServletRequest req) {
		return HttpRespostas.unprocessable(
				ErroResposta.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(NaoAutenticadoException.class)
	public ResponseEntity<ErroResposta> naoAutenticado(NaoAutenticadoException ex, HttpServletRequest req) {
		return HttpRespostas.unauthorized(
				ErroResposta.of(HttpStatus.UNAUTHORIZED, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErroResposta> autenticacaoSpring(AuthenticationException ex, HttpServletRequest req) {
		return HttpRespostas.unauthorized(
				ErroResposta.of(HttpStatus.UNAUTHORIZED, "Autenticação necessária", path(req)));
	}

	@ExceptionHandler({ OperacaoNaoPermitidaException.class, AccessDeniedException.class })
	public ResponseEntity<ErroResposta> proibido(Exception ex, HttpServletRequest req) {
		String mensagem = ex instanceof OperacaoNaoPermitidaException ? ex.getMessage() : "Acesso negado";
		return HttpRespostas.forbidden(ErroResposta.of(HttpStatus.FORBIDDEN, mensagem, path(req)));
	}

	@ExceptionHandler(LimiteDeTentativasException.class)
	public ResponseEntity<ErroResposta> muitasTentativas(LimiteDeTentativasException ex, HttpServletRequest req) {
		return HttpRespostas.tooManyRequests(
				ErroResposta.of(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), path(req)));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErroResposta> metodoNaoPermitido(
			HttpRequestMethodNotSupportedException ex,
			HttpServletRequest req) {
		return HttpRespostas.methodNotAllowed(
				ErroResposta.of(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido", path(req)));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErroResposta> arquivoGrande(MaxUploadSizeExceededException ex, HttpServletRequest req) {
		return HttpRespostas.payloadTooLarge(
				ErroResposta.of(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo excede o tamanho máximo", path(req)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResposta> inesperado(Exception ex, HttpServletRequest req) {
		return HttpRespostas.internalError(
				ErroResposta.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", path(req)));
	}

	private String campo(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private String path(HttpServletRequest req) {
		return req.getRequestURI();
	}
}
