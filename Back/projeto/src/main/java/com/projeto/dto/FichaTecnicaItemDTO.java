package com.projeto.dto;

import java.math.BigDecimal;

import com.projeto.entity.FichaTecnicaItem;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public final class FichaTecnicaItemDTO {

	private FichaTecnicaItemDTO() {
	}

	public record Create(
			@NotNull Long produtoId,
			@NotNull Long insumoId,
			@NotNull @DecimalMin("0.0001") BigDecimal quantidade) {
	}

	public record Response(Long id, Long produtoId, Long insumoId, BigDecimal quantidade) {

		public static Response from(FichaTecnicaItem item) {
			return new Response(
					item.getId(),
					item.getProduto().getId(),
					item.getInsumo().getId(),
					item.getQuantidade());
		}
	}
}
