package com.projeto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.projeto.entity.enums.ClasseMenu;

public final class DashboardDTO {

	private DashboardDTO() {
	}

	public record PontoDia(LocalDate data, BigDecimal valor) {
	}

	public record TopItem(Long produtoId, String nome, long quantidade) {
	}

	public record Response(
			BigDecimal faturamento,
			long totalPedidos,
			BigDecimal ticketMedio,
			List<PontoDia> faturamentoPorDia,
			List<TopItem> topItens,
			Map<ClasseMenu, Integer> itensPorClasse) {
	}
}
