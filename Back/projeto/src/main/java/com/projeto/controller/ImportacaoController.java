package com.projeto.controller;

import com.projeto.service.ImportacaoService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.dto.ImportacaoDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

@RestController
@RequestMapping("/api/importacoes")
public class ImportacaoController {

	private final ImportacaoService importacaoService;
	private final SessaoAtual sessaoAtual;

	public ImportacaoController(ImportacaoService importacaoService, SessaoAtual sessaoAtual) {
		this.importacaoService = importacaoService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<ImportacaoDTO.Response> listar(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(importacaoService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public ImportacaoDTO.Response buscar(@PathVariable Long id) {
		return importacaoService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping("/pre-visualizar")
	public ImportacaoDTO.Previa previsualizar(
			@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam("estabelecimentoId") Long estabelecimentoId) {
		return importacaoService.previsualizar(arquivo, estabelecimentoId, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<ImportacaoDTO.Response> confirmar(
			@RequestParam("arquivo") MultipartFile arquivo,
			@RequestParam("estabelecimentoId") Long estabelecimentoId) {
		ImportacaoDTO.Response corpo = importacaoService.confirmar(arquivo, estabelecimentoId, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/importacoes/" + corpo.id()), corpo);
	}
}
