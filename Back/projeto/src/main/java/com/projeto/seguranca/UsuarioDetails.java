package com.projeto.seguranca;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.projeto.entity.Usuario;
import com.projeto.entity.enums.Perfil;

public class UsuarioDetails implements UserDetails {

	private final Long id;
	private final Long estabelecimentoId;
	private final String email;
	private final String senhaHash;
	private final Perfil perfil;
	private final boolean ativo;
	private final List<GrantedAuthority> autoridades;

	public UsuarioDetails(Usuario usuario) {
		this.id = usuario.getId();
		this.estabelecimentoId = usuario.getEstabelecimento().getId();
		this.email = usuario.getEmail();
		this.senhaHash = usuario.getSenhaHash();
		this.perfil = usuario.getPerfil();
		this.ativo = usuario.isAtivo();
		this.autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
	}

	public UsuarioAtual atual() {
		return new UsuarioAtual(id, estabelecimentoId, perfil, email);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return autoridades;
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return ativo;
	}
}
