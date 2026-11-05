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
			String categoria,
			long quantidade,
			BigDecimal popularidade,
			BigDecimal margem,
			ClasseMenu classe,
			String recomendacao) {
	}

	public record Response(
			long quantidadePedidos,
			BigDecimal receita,
			BigDecimal ticketMedio,
			BigDecimal limitePopularidade,
			BigDecimal margemMedia,
			List<Item> itens) {
	}
}
