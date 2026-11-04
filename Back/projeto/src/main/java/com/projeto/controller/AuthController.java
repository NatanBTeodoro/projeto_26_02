package com.projeto.controller;

import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.LoginRequest;
import com.projeto.dto.UsuarioDTO;
import com.projeto.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
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

	@GetMapping("/eu")
	public UsuarioDTO.Response eu() {
		return authService.eu();
	}
}
