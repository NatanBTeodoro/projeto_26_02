package com.projeto.dto;

import java.math.BigDecimal;

import com.projeto.entity.Insumo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class InsumoDTO {

	private InsumoDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@NotBlank @Size(max = 150) String nome,
			@NotBlank @Size(max = 30) String unidadeMedida,
			BigDecimal custoUnitario) {
	}

	public record Update(
			@NotBlank @Size(max = 150) String nome,
			@NotBlank @Size(max = 30) String unidadeMedida,
			BigDecimal custoUnitario,
			Boolean ativo) {
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			String nome,
			String unidadeMedida,
			BigDecimal custoUnitario,
			boolean ativo) {

		public static Response from(Insumo i) {
			return new Response(
					i.getId(),
					i.getEstabelecimento().getId(),
					i.getNome(),
					i.getUnidadeMedida(),
					i.getCustoUnitario(),
					i.isAtivo());
		}
	}
}
