package com.projeto.dto;

import com.projeto.entity.Alerta;
import com.projeto.entity.enums.TipoAlerta;

public final class AlertaDTO {

	private AlertaDTO() {
	}

	public record Response(Long id, Long produtoId, TipoAlerta tipo, Integer periodos, boolean ativo) {

		public static Response from(Alerta a) {
			return new Response(a.getId(), a.getProduto().getId(), a.getTipo(), a.getPeriodos(), a.isAtivo());
		}
	}
}
