package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.Produto;
import com.projeto.entity.enums.StatusPedido;

class LeituraVendasTest {

	@Test
	void pedidoEstornadoNaoEntraNaQuantidadeNemNaReceita() {
		Pedido ativo = pedido(StatusPedido.ATIVO, 2, "10.00");
		Pedido estornado = pedido(StatusPedido.ESTORNADO, 5, "30.00");

		List<ClassificadorCardapio.Linha> linhas = LeituraVendas.linhasAtivas(List.of(ativo, estornado));

		assertEquals(1, LeituraVendas.contarPedidosAtivos(List.of(ativo, estornado)));
		assertEquals(1, linhas.size());
		assertEquals(2, linhas.get(0).quantidade());
		assertEquals(new BigDecimal("20.00"), ClassificadorCardapio.receita(linhas));
	}

	private static Pedido pedido(StatusPedido status, int quantidade, String preco) {
		Produto produto = new Produto();
		produto.setId(1L);
		produto.setNome("Prato");
		Pedido pedido = new Pedido();
		pedido.setStatus(status);
		pedido.setDataHora(Instant.parse("2026-01-10T15:00:00Z"));
		PedidoItem item = new PedidoItem();
		item.setPedido(pedido);
		item.setProduto(produto);
		item.setQuantidade(quantidade);
		item.setPrecoUnitario(new BigDecimal(preco));
		item.setCustoUnitario(new BigDecimal("4.00"));
		pedido.getItens().add(item);
		return pedido;
	}
}
