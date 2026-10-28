package com.projeto.dto;

import java.math.BigDecimal;

import com.projeto.entity.ConfigAnalise;
import com.projeto.entity.enums.CriterioPopularidade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class ConfigAnaliseDTO {

	private ConfigAnaliseDTO() {
	}

	public record Update(
			@NotNull @DecimalMin("0") BigDecimal suporteMin,
			@NotNull @DecimalMin("0") BigDecimal confiancaMin,
			@NotNull @DecimalMin("0") BigDecimal liftMin,
			@NotNull @Min(1) Integer periodoMinimoDias,
			@NotNull @Min(1) Integer nAlertaCao,
			@NotNull CriterioPopularidade criterioPopularidade,
			@NotNull Boolean escopoPorCategoria) {
	}

	public record Response(
			Long id,
			Long estabelecimentoId,
			BigDecimal suporteMin,
			BigDecimal confiancaMin,
			BigDecimal liftMin,
			Integer periodoMinimoDias,
			Integer nAlertaCao,
			CriterioPopularidade criterioPopularidade,
			boolean escopoPorCategoria) {

		public static Response from(ConfigAnalise c) {
			return new Response(
					c.getId(),
					c.getEstabelecimento().getId(),
					c.getSuporteMin(),
					c.getConfiancaMin(),
					c.getLiftMin(),
					c.getPeriodoMinimoDias(),
					c.getNAlertaCao(),
					c.getCriterioPopularidade(),
					c.isEscopoPorCategoria());
		}
	}
}
