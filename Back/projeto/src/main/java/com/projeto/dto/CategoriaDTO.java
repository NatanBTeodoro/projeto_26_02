package com.projeto.dto;

import com.projeto.entity.Categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDTO {

	private CategoriaDTO() {
	}

	public record Create(
			@NotBlank(message = "Informe o nome") @Size(max = 120) String nome) {
	}

	public record Update(
			@NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
			Boolean ativa) {
	}

	public record Response(Long id, String nome, boolean ativa) {

		public static Response de(Categoria categoria) {
			return new Response(categoria.getId(), categoria.getNome(), categoria.isAtiva());
		}
	}
}
