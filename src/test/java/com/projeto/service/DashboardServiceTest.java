package com.projeto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.projeto.dto.DashboardDTO;
import com.projeto.entity.Categoria;
import com.projeto.entity.Pedido;
import com.projeto.entity.PedidoItem;
import com.projeto.entity.Produto;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.StatusPedido;

class DashboardServiceTest {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	@Test
	void tresPedidosBatemComAContaManual() {
		Produto pizza = produto(1L, "Pizza");
		Produto suco = produto(2L, "Suco");
		List<Pedido> pedidos = List.of(
				pedido(LocalDate.of(2026, 10, 1), 15, StatusPedido.ATIVO, pizza, 2, "20.00", "10.00"),
				pedido(LocalDate.of(2026, 10, 1), 18, StatusPedido.ATIVO, suco, 1, "8.00", "3.00"),
				pedido(LocalDate.of(2026, 10, 3), 12, StatusPedido.ATIVO, pizza, 1, "20.00", "10.00"),
				pedido(LocalDate.of(2026, 10, 3), 13, StatusPedido.ESTORNADO, pizza, 9, "20.00", "10.00"));

		DashboardDTO.Response painel = DashboardService.montar(
				LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 10, 3),
				pedidos);

		assertEquals(new BigDecimal("68.00"), painel.faturamento());
		assertEquals(3, painel.totalPedidos());
		assertEquals(new BigDecimal("22.67"), painel.ticketMedio());
		assertEquals(3, painel.faturamentoPorDia().size());
		assertEquals(new BigDecimal("48.00"), painel.faturamentoPorDia().get(0).valor());
		assertEquals(new BigDecimal("0.00"), painel.faturamentoPorDia().get(1).valor());
		assertEquals(new BigDecimal("20.00"), painel.faturamentoPorDia().get(2).valor());
		assertEquals(2, painel.topItens().size());
		assertEquals("Pizza", painel.topItens().get(0).nome());
		assertEquals(3, painel.topItens().get(0).quantidade());
		assertEquals(1, painel.itensPorClasse().get(ClasseMenu.ESTRELA));
		assertEquals(1, painel.itensPorClasse().get(ClasseMenu.CAO));
		assertEquals(0, painel.itensPorClasse().get(ClasseMenu.ENIGMA));
	}

	@Test
	void periodoSemPedidosFicaZerado() {
		DashboardDTO.Response painel = DashboardService.montar(
				LocalDate.of(2026, 10, 2),
				LocalDate.of(2026, 10, 2),
				List.of());

		assertEquals(new BigDecimal("0.00"), painel.faturamento());
		assertEquals(0, painel.totalPedidos());
		assertEquals(new BigDecimal("0.00"), painel.ticketMedio());
		assertEquals(1, painel.faturamentoPorDia().size());
		assertEquals(new BigDecimal("0.00"), painel.faturamentoPorDia().get(0).valor());
		assertEquals(0, painel.topItens().size());
		assertEquals(0, painel.itensPorClasse().get(ClasseMenu.ESTRELA));
	}

	private static Produto produto(Long id, String nome) {
		Categoria categoria = new Categoria();
		categoria.setNome("Cardápio");
		Produto produto = new Produto();
		produto.setId(id);
		produto.setNome(nome);
		produto.setCategoria(categoria);
		return produto;
	}

	private static Pedido pedido(
			LocalDate dia,
			int hora,
			StatusPedido status,
			Produto produto,
			int quantidade,
			String preco,
			String custo) {
		Pedido pedido = new Pedido();
		pedido.setStatus(status);
		pedido.setDataHora(ZonedDateTime.of(dia.getYear(), dia.getMonthValue(), dia.getDayOfMonth(), hora, 0, 0, 0, FUSO)
				.toInstant());
		PedidoItem item = new PedidoItem();
		item.setPedido(pedido);
		item.setProduto(produto);
		item.setQuantidade(quantidade);
		item.setPrecoUnitario(new BigDecimal(preco));
		item.setCustoUnitario(new BigDecimal(custo));
		pedido.getItens().add(item);
		return pedido;
	}
}
