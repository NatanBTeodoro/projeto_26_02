package com.projeto.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class RelatorioDTO {

	private RelatorioDTO() {
	}

	public record Dashboard(
			BigDecimal receita,
			BigDecimal ticketMedio,
			long pedidos,
			Map<String, Long> itensPorClasse,
			long alertas,
			List<AnaliseDTO.RegraResponse> melhoresRegras,
			boolean baseInsuficiente) {
	}

	public record AbcItem(
			Long produtoId,
			String nome,
			String categoria,
			BigDecimal receita,
			BigDecimal margem,
			BigDecimal participacao,
			String classeAbc) {
	}

	public record AbcCategoria(
			String categoria,
			BigDecimal receita,
			BigDecimal margem,
			BigDecimal participacaoReceita,
			BigDecimal participacaoMargem) {
	}

	public record Abc(List<AbcItem> itens, List<AbcCategoria> categorias) {
	}

	public record Cliente(
			String cliente,
			long pedidos,
			BigDecimal ticketMedio,
			BigDecimal intervaloMedioHoras) {
	}

	public record UnidadeDesempenho(
			Long unidadeId,
			String nome,
			BigDecimal receita,
			long pedidos,
			BigDecimal ticketMedio) {
	}
}
