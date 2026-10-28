package com.projeto.dto;

import java.time.Instant;

import com.projeto.entity.Auditoria;

public final class AuditoriaDTO {

	private AuditoriaDTO() {
	}

	public record Response(
			Long id,
			Long usuarioId,
			String acao,
			String entidade,
			Long entidadeId,
			String valorAnterior,
			String valorNovo,
			Instant dataHora) {

		public static Response from(Auditoria a) {
			return new Response(
					a.getId(),
					a.getUsuario() != null ? a.getUsuario().getId() : null,
					a.getAcao(),
					a.getEntidade(),
					a.getEntidadeId(),
					a.getValorAnterior(),
					a.getValorNovo(),
					a.getDataHora());
		}
	}
}
