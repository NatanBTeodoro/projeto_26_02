package com.projeto.service;

import com.projeto.dto.LoginRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.projeto.dto.UsuarioDTO;
import com.projeto.seguranca.ContadorTentativasLogin;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.seguranca.UsuarioDetails;
import com.projeto.util.exception.NaoAutenticadoException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final ContadorTentativasLogin contador;
	private final SessaoAtual sessaoAtual;

	public AuthService(
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			ContadorTentativasLogin contador,
			SessaoAtual sessaoAtual) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.contador = contador;
		this.sessaoAtual = sessaoAtual;
	}

	public UsuarioDTO.Response entrar(LoginRequest pedido, HttpServletRequest request, HttpServletResponse response) {
		String email = pedido.email().trim().toLowerCase();
		contador.verificar(email);
		Authentication autenticado;
		try {
			autenticado = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, pedido.senha()));
		} catch (AuthenticationException ex) {
			contador.registrarFalha(email);
			throw new NaoAutenticadoException("Usuário ou senha inválidos.");
		}
		contador.zerar(email);

		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(autenticado);
		SecurityContextHolder.setContext(contexto);
		request.getSession(true);
		request.changeSessionId();
		securityContextRepository.saveContext(contexto, request, response);

		return resposta((UsuarioDetails) autenticado.getPrincipal());
	}

	public UsuarioDTO.Response sessao() {
		return respostaDe(sessaoAtual.exigir());
	}

	private UsuarioDTO.Response resposta(UsuarioDetails detalhes) {
		return respostaDe(detalhes.atual());
	}

	private UsuarioDTO.Response respostaDe(UsuarioAtual atual) {
		return new UsuarioDTO.Response(atual.id(), atual.estabelecimentoId(), atual.email(), atual.perfil(), true);
	}
}
