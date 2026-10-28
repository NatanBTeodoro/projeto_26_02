package com.projeto.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.projeto.util.exception.LimiteDeTentativasException;
import com.projeto.util.exception.NaoAutenticadoException;
import com.projeto.util.exception.OperacaoNaoPermitidaException;
import com.projeto.util.exception.RecursoJaExistenteException;
import com.projeto.util.exception.RecursoNaoEncontradoException;
import com.projeto.util.exception.RegraDeNegocioException;
import com.projeto.util.exception.RequisicaoInvalidaException;

import jakarta.servlet.http.HttpServletRequest;

class TratadorExcecoesTest {

	private TratadorExcecoes tratador;
	private HttpServletRequest request;

	@BeforeEach
	void preparar() {
		tratador = new TratadorExcecoes();
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/produtos/9");
	}

	@Test
	@DisplayName("Recurso inexistente responde 404")
	void naoEncontradoRetorna404() {
		ResponseEntity<ErroResposta> resp = tratador.naoEncontrado(
				new RecursoNaoEncontradoException("Produto", 9L), request);

		assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
		assertEquals(404, resp.getBody().status());
		assertEquals("/api/produtos/9", resp.getBody().caminho());
	}

	@Test
	@DisplayName("Requisição inválida responde 400")
	void invalidaRetorna400() {
		ResponseEntity<ErroResposta> resp = tratador.requisicaoInvalida(
				new RequisicaoInvalidaException("Quantidade deve ser maior que zero"), request);

		assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
	}

	@Test
	@DisplayName("Não autenticado responde 401")
	void naoAutenticadoRetorna401() {
		ResponseEntity<ErroResposta> resp = tratador.naoAutenticado(
				new NaoAutenticadoException("sessão"), request);

		assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
	}

	@Test
	@DisplayName("Operação negada responde 403")
	void proibidoRetorna403() {
		ResponseEntity<ErroResposta> resp = tratador.proibido(
				new OperacaoNaoPermitidaException("Perfil sem permissão"), request);

		assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
		assertEquals("Perfil sem permissão", resp.getBody().mensagem());
	}

	@Test
	@DisplayName("Registro duplicado responde 409")
	void conflitoRetorna409() {
		ResponseEntity<ErroResposta> resp = tratador.conflito(
				new RecursoJaExistenteException("E-mail já cadastrado"), request);

		assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
	}

	@Test
	@DisplayName("Regra de negócio responde 422")
	void regraRetorna422() {
		ResponseEntity<ErroResposta> resp = tratador.regra(
				new RegraDeNegocioException("Produto inativo não entra em pedido"), request);

		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resp.getStatusCode());
	}

	@Test
	@DisplayName("Limite de tentativas responde 429")
	void limiteRetorna429() {
		ResponseEntity<ErroResposta> resp = tratador.muitasTentativas(
				new LimiteDeTentativasException("Aguarde 5 minutos"), request);

		assertEquals(HttpStatus.TOO_MANY_REQUESTS, resp.getStatusCode());
	}

	@Test
	@DisplayName("Falha inesperada responde 500 sem vazar causa")
	void inesperadoRetorna500() {
		ResponseEntity<ErroResposta> resp = tratador.inesperado(new RuntimeException("segredo interno"), request);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
		assertEquals("Erro interno", resp.getBody().mensagem());
	}

	@Test
	@DisplayName("HttpRespostas.ok devolve 200")
	void okRetorna200() {
		ResponseEntity<String> resp = HttpRespostas.ok("ok");

		assertEquals(HttpStatus.OK, resp.getStatusCode());
		assertEquals("ok", resp.getBody());
	}
}
