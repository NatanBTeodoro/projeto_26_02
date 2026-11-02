package com.projeto.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.PageResponseDTO;
import com.projeto.dto.UnidadeDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.service.UnidadeService;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeController {

	private final UnidadeService unidadeService;
	private final SessaoAtual sessaoAtual;

	public UnidadeController(UnidadeService unidadeService, SessaoAtual sessaoAtual) {
		this.unidadeService = unidadeService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<UnidadeDTO.Response> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
		return PageResponseDTO.from(unidadeService.listar(pageable, sessaoAtual.exigir()));
	}

	@PostMapping
	public ResponseEntity<UnidadeDTO.Response> criar(@Valid @RequestBody UnidadeDTO.Create dto) {
		UnidadeDTO.Response corpo = unidadeService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/unidades/" + corpo.id()), corpo);
	}
}
