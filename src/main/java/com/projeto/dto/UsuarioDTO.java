package com.projeto.dto;

import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.seguranca.UsuarioDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UsuarioDTO {

	private UsuarioDTO() {
	}

	public record Create(
			@NotBlank(message = "Informe o e-mail") @Email(message = "Informe um e-mail válido") @Size(max = 255) String email,
			@NotBlank(message = "Informe a senha") @Size(min = 6, max = 72, message = "A senha precisa ter entre 6 e 72 caracteres") String senha,
			@NotNull(message = "Informe o perfil") Perfil perfil) {
	}

	public record Update(
			@Email(message = "Informe um e-mail válido") @Size(max = 255) String email,
			@Size(min = 6, max = 72, message = "A senha precisa ter entre 6 e 72 caracteres") String senha,
			Perfil perfil,
			Boolean ativo) {
	}

	public record Sessao(Long id, String email, Perfil perfil, boolean ativo, String token) {

		public static Sessao de(Usuario usuario) {
			return new Sessao(usuario.getId(), usuario.getEmail(), usuario.getPerfil(), usuario.isAtivo(), usuario.getTokenAcesso());
		}
	}

	public record Response(Long id, String email, Perfil perfil, boolean ativo) {

		public static Response de(Usuario usuario) {
			return new Response(usuario.getId(), usuario.getEmail(), usuario.getPerfil(), usuario.isAtivo());
		}

		public static Response de(UsuarioDetails detalhes) {
			UsuarioAtual atual = detalhes.atual();
			return new Response(atual.id(), atual.email(), atual.perfil(), true);
		}

		public static Response de(UsuarioAtual atual) {
			return new Response(atual.id(), atual.email(), atual.perfil(), true);
		}
	}
}
