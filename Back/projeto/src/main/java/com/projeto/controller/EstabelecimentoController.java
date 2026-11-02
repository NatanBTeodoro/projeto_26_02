package com.projeto.controller;

import com.projeto.service.EstabelecimentoService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.EstabelecimentoDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/estabelecimentos")
public class EstabelecimentoController {

	private final EstabelecimentoService estabelecimentoService;
	private final SessaoAtual sessaoAtual;

	public EstabelecimentoController(EstabelecimentoService estabelecimentoService, SessaoAtual sessaoAtual) {
		this.estabelecimentoService = estabelecimentoService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<EstabelecimentoDTO.Response> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
		return PageResponseDTO.from(estabelecimentoService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public EstabelecimentoDTO.Response buscar(@PathVariable Long id) {
		return estabelecimentoService.buscar(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<EstabelecimentoDTO.Response> criar(@Valid @RequestBody EstabelecimentoDTO.Create dto) {
		EstabelecimentoDTO.Response corpo = estabelecimentoService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/estabelecimentos/" + corpo.id()), corpo);
	}

	@PutMapping("/{id}")
	public EstabelecimentoDTO.Response atualizar(
			@PathVariable Long id,
			@Valid @RequestBody EstabelecimentoDTO.Update dto) {
		return estabelecimentoService.atualizar(id, dto, sessaoAtual.exigir());
	}
}
