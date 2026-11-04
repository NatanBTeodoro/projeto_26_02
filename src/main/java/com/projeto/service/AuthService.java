package com.projeto.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.LoginRequest;
import com.projeto.dto.UsuarioDTO;
import com.projeto.entity.Usuario;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.seguranca.UsuarioDetails;
import com.projeto.util.exception.NaoAutenticadoException;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UsuarioRepository usuarioRepository;
	private final SessaoAtual sessaoAtual;

	public AuthService(
			AuthenticationManager authenticationManager,
			UsuarioRepository usuarioRepository,
			SessaoAtual sessaoAtual) {
		this.authenticationManager = authenticationManager;
		this.usuarioRepository = usuarioRepository;
		this.sessaoAtual = sessaoAtual;
	}

	@Transactional
	public UsuarioDTO.Sessao entrar(LoginRequest pedido) {
		String email = pedido.email().trim().toLowerCase(Locale.ROOT);
		Authentication autenticado;
		try {
			autenticado = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, pedido.senha()));
		} catch (AuthenticationException ex) {
			throw new NaoAutenticadoException("Usuário ou senha inválidos");
		}

		UsuarioDetails detalhes = (UsuarioDetails) autenticado.getPrincipal();
		Usuario usuario = usuarioRepository.findById(detalhes.atual().id())
				.orElseThrow(() -> new NaoAutenticadoException("Usuário ou senha inválidos"));
		usuario.setTokenAcesso(UUID.randomUUID().toString());
		return UsuarioDTO.Sessao.de(usuarioRepository.save(usuario));
	}

	public UsuarioDTO.Response eu() {
		return UsuarioDTO.Response.de(sessaoAtual.exigir());
	}

	@Transactional
	public void sair() {
		Long id = sessaoAtual.exigir().id();
		usuarioRepository.findById(id).ifPresent(usuario -> {
			usuario.setTokenAcesso(null);
			usuarioRepository.save(usuario);
		});
	}
}
