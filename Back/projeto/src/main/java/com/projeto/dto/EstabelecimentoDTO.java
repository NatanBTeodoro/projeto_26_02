package com.projeto.dto;

import com.projeto.entity.Estabelecimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class EstabelecimentoDTO {

	private EstabelecimentoDTO() {
	}

	public record Create(
			@NotBlank @Size(max = 150) String nome) {
	}

	public record Update(
			@NotBlank @Size(max = 150) String nome,
			Boolean ativo) {
	}

	public record Response(Long id, String nome, boolean ativo) {

		public static Response from(Estabelecimento e) {
			return new Response(e.getId(), e.getNome(), e.isAtivo());
		}
	}
}
