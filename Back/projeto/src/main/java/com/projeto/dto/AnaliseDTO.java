package com.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.projeto.entity.AnaliseExecucao;
import com.projeto.entity.ResultadoClassificacao;
import com.projeto.entity.ResultadoRegra;
import com.projeto.entity.enums.ClasseMenu;
import com.projeto.entity.enums.Granularidade;
import com.projeto.entity.enums.StatusAnalise;

import jakarta.validation.constraints.NotNull;

public final class AnaliseDTO {

	private AnaliseDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@NotNull Instant periodoIni,
			@NotNull Instant periodoFim,
			@NotNull Granularidade granularidade) {
	}

	public record ExecucaoResponse(
			Long id,
			Long estabelecimentoId,
			Instant periodoIni,
			Instant periodoFim,
			Granularidade granularidade,
			String versaoDados,
			StatusAnalise status,
			Instant iniciadoEm,
			Instant concluidoEm,
			String erro) {

		public static ExecucaoResponse from(AnaliseExecucao e) {
			return new ExecucaoResponse(
					e.getId(),
					e.getEstabelecimento().getId(),
					e.getPeriodoIni(),
					e.getPeriodoFim(),
					e.getGranularidade(),
					e.getVersaoDados(),
					e.getStatus(),
					e.getIniciadoEm(),
					e.getConcluidoEm(),
					e.getErro());
		}
	}

	public record ClassificacaoResponse(
			Long id,
			Long produtoId,
			BigDecimal popularidade,
			BigDecimal margem,
			ClasseMenu classe) {

		public static ClassificacaoResponse from(ResultadoClassificacao r) {
			return new ClassificacaoResponse(
					r.getId(),
					r.getProduto().getId(),
					r.getPopularidade(),
					r.getMargem(),
					r.getClasse());
		}
	}

	public record RegraResponse(
			Long id,
			String antecedente,
			String consequente,
			BigDecimal suporte,
			BigDecimal confianca,
			BigDecimal lift,
			boolean fragil) {

		public static RegraResponse from(ResultadoRegra r) {
			return new RegraResponse(
					r.getId(),
					r.getAntecedente(),
					r.getConsequente(),
					r.getSuporte(),
					r.getConfianca(),
					r.getLift(),
					r.isFragil());
		}
	}
}
