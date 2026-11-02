package com.projeto.dto;

import com.projeto.entity.Unidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UnidadeDTO {

	private UnidadeDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@NotBlank @Size(max = 150) String nome) {
	}

	public record Response(Long id, Long estabelecimentoId, String nome, boolean ativa) {

		public static Response from(Unidade unidade) {
			return new Response(
					unidade.getId(),
					unidade.getEstabelecimento().getId(),
					unidade.getNome(),
					unidade.isAtiva());
		}
	}
}
