package com.projeto.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.projeto.dto.LoginRequest;
import com.projeto.dto.UsuarioDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.seguranca.UsuarioDetails;
import com.projeto.util.exception.NaoAutenticadoException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final SessaoAtual sessaoAtual;

	public AuthService(
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			SessaoAtual sessaoAtual) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.sessaoAtual = sessaoAtual;
	}

	public UsuarioDTO.Response entrar(LoginRequest pedido, HttpServletRequest request, HttpServletResponse response) {
		String email = pedido.email().trim().toLowerCase(Locale.ROOT);
		Authentication autenticado;
		try {
			autenticado = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, pedido.senha()));
		} catch (AuthenticationException ex) {
			throw new NaoAutenticadoException("Usuário ou senha inválidos");
		}

		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(autenticado);
		SecurityContextHolder.setContext(contexto);
		request.getSession(true);
		request.changeSessionId();
		securityContextRepository.saveContext(contexto, request, response);
		return UsuarioDTO.Response.de((UsuarioDetails) autenticado.getPrincipal());
	}

	public UsuarioDTO.Response eu() {
		return UsuarioDTO.Response.de(sessaoAtual.exigir());
	}
}
