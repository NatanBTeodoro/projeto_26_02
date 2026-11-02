package com.projeto.controller;

import com.projeto.service.AuthService;
import com.projeto.service.RecuperacaoSenhaService;
import com.projeto.dto.LoginRequest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.UsuarioDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final RecuperacaoSenhaService recuperacaoSenhaService;

	public AuthController(AuthService authService, RecuperacaoSenhaService recuperacaoSenhaService) {
		this.authService = authService;
		this.recuperacaoSenhaService = recuperacaoSenhaService;
	}

	@GetMapping("/csrf")
	public Map<String, String> csrf(CsrfToken token) {
		return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
	}

	@PostMapping("/login")
	public UsuarioDTO.Response login(
			@Valid @RequestBody LoginRequest pedido,
			HttpServletRequest request,
			HttpServletResponse response) {
		return authService.entrar(pedido, request, response);
	}

	@GetMapping("/sessao")
	public UsuarioDTO.Response sessao() {
		return authService.sessao();
	}

	@PostMapping("/recuperar-senha")
	public RecuperacaoSenhaService.RecuperacaoResponse recuperar(
			@Valid @RequestBody RecuperacaoSenhaService.Pedido pedido) {
		return recuperacaoSenhaService.solicitar(pedido);
	}

	@PostMapping("/redefinir-senha")
	public ResponseEntity<Void> redefinir(@Valid @RequestBody RecuperacaoSenhaService.Redefinicao pedido) {
		recuperacaoSenhaService.redefinir(pedido);
		return ResponseEntity.noContent().build();
	}
}
