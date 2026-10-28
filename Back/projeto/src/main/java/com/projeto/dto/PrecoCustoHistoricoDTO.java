package com.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.projeto.entity.PrecoCustoHistorico;

public final class PrecoCustoHistoricoDTO {

	private PrecoCustoHistoricoDTO() {
	}

	public record Response(
			Long id,
			Long produtoId,
			BigDecimal preco,
			BigDecimal custo,
			Instant vigenciaIni,
			Instant vigenciaFim) {

		public static Response from(PrecoCustoHistorico h) {
			return new Response(
					h.getId(),
					h.getProduto().getId(),
					h.getPreco(),
					h.getCusto(),
					h.getVigenciaIni(),
					h.getVigenciaFim());
		}
	}
}
