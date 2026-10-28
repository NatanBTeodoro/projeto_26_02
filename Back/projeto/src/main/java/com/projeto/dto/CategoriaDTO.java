package com.projeto.dto;

import com.projeto.entity.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CategoriaDTO {

	private CategoriaDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@NotBlank @Size(max = 120) String nome) {
	}

	public record Update(
			@NotBlank @Size(max = 120) String nome,
			Boolean ativa) {
	}

	public record Response(Long id, Long estabelecimentoId, String nome, boolean ativa) {

		public static Response from(Categoria c) {
			return new Response(c.getId(), c.getEstabelecimento().getId(), c.getNome(), c.isAtiva());
		}
	}
}
