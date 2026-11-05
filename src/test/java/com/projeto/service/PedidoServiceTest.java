package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.dto.PedidoDTO;

class PedidoServiceTest {

	@Test
	void mesmoProdutoNoPedidoSomaAQuantidade() {
		var quantidades = PedidoService.agrupar(List.of(
				new PedidoDTO.ItemCreate(7L, 2),
				new PedidoDTO.ItemCreate(3L, 1),
				new PedidoDTO.ItemCreate(7L, 3)));

		assertEquals(2, quantidades.size());
		assertEquals(5, quantidades.get(7L));
		assertEquals(1, quantidades.get(3L));
	}
}
