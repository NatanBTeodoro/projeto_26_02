package com.projeto.dto;

import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UsuarioDTO {

	private UsuarioDTO() {
	}

	public record Create(
			@NotNull Long estabelecimentoId,
			@NotBlank @Email @Size(max = 255) String email,
			@NotBlank @Size(min = 8, max = 72) String senha,
			@NotNull Perfil perfil) {
	}

	public record Update(
			@Email @Size(max = 255) String email,
			@Size(min = 8, max = 72) String senha,
			Perfil perfil,
			Boolean ativo) {
	}

	public record Response(Long id, Long estabelecimentoId, String email, Perfil perfil, boolean ativo) {

		public static Response from(Usuario u) {
			return new Response(
					u.getId(),
					u.getEstabelecimento().getId(),
					u.getEmail(),
					u.getPerfil(),
					u.isAtivo());
		}
	}
}
