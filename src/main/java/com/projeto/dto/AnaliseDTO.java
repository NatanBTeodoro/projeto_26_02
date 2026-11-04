package com.projeto.dto;

import java.math.BigDecimal;
import java.util.List;

import com.projeto.entity.enums.ClasseMenu;

public final class AnaliseDTO {

	private AnaliseDTO() {
	}

	public record Item(
			Long produtoId,
			String nome,
			long quantidade,
			BigDecimal popularidade,
			BigDecimal margem,
			ClasseMenu classe) {
	}

	public record Response(
			long quantidadePedidos,
			BigDecimal receita,
			BigDecimal ticketMedio,
			List<Item> itens) {
	}
}
