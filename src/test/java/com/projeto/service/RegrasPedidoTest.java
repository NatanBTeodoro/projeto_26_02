package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.dto.PedidoDTO;
import com.projeto.util.exception.RegraDeNegocioException;

class RegrasPedidoTest {

	private final Instant agora = Instant.parse("2026-06-01T12:00:00Z");

	@Test
	void rejeitaPedidoVazio() {
		RegraDeNegocioException erro = assertThrows(
				RegraDeNegocioException.class,
				() -> RegrasPedido.validar(agora, List.of(), agora));
		assertEquals("O pedido precisa de ao menos um item", erro.getMessage());
	}

	@Test
	void rejeitaQuantidadeZero() {
		RegraDeNegocioException erro = assertThrows(
				RegraDeNegocioException.class,
				() -> RegrasPedido.validar(agora, List.of(new PedidoDTO.ItemCreate(1L, 0)), agora));
		assertEquals("A quantidade precisa ser maior que zero", erro.getMessage());
	}
}
