package com.projeto.controller;

import com.projeto.service.UsuarioService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.PageResponseDTO;
import com.projeto.dto.UsuarioDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;
	private final SessaoAtual sessaoAtual;

	public UsuarioController(UsuarioService usuarioService, SessaoAtual sessaoAtual) {
		this.usuarioService = usuarioService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<UsuarioDTO.Response> listar(
			@PageableDefault(size = 20, sort = "email") Pageable pageable) {
		return PageResponseDTO.from(usuarioService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public UsuarioDTO.Response buscar(@PathVariable Long id) {
		return usuarioService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<UsuarioDTO.Response> criar(@Valid @RequestBody UsuarioDTO.Create dto) {
		UsuarioDTO.Response corpo = usuarioService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/usuarios/" + corpo.id()), corpo);
	}

	@PutMapping("/{id}")
	public UsuarioDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO.Update dto) {
		return usuarioService.atualizar(id, dto, sessaoAtual.exigir());
	}
}
