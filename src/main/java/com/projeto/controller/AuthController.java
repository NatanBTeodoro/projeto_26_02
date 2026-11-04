package com.projeto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.LoginRequest;
import com.projeto.dto.UsuarioDTO;
import com.projeto.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public UsuarioDTO.Sessao login(@Valid @RequestBody LoginRequest pedido) {
		return authService.entrar(pedido);
	}

	@GetMapping("/eu")
	public UsuarioDTO.Response eu() {
		return authService.eu();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		authService.sair();
		return ResponseEntity.noContent().build();
	}
}
