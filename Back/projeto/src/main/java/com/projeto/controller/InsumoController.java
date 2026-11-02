package com.projeto.controller;

import com.projeto.service.InsumoService;

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

import com.projeto.dto.InsumoDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

	private final InsumoService insumoService;
	private final SessaoAtual sessaoAtual;

	public InsumoController(InsumoService insumoService, SessaoAtual sessaoAtual) {
		this.insumoService = insumoService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<InsumoDTO.Response> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
		return PageResponseDTO.from(insumoService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public InsumoDTO.Response buscar(@PathVariable Long id) {
		return insumoService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<InsumoDTO.Response> criar(@Valid @RequestBody InsumoDTO.Create dto) {
		InsumoDTO.Response corpo = insumoService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/insumos/" + corpo.id()), corpo);
	}

	@PutMapping("/{id}")
	public InsumoDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody InsumoDTO.Update dto) {
		return insumoService.atualizar(id, dto, sessaoAtual.exigir());
	}
}
