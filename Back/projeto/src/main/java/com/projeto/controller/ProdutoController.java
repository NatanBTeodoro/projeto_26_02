package com.projeto.controller;

import com.projeto.service.ProdutoService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.projeto.dto.PrecoCustoHistoricoDTO;
import com.projeto.dto.ProdutoDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

	private final ProdutoService produtoService;
	private final SessaoAtual sessaoAtual;

	public ProdutoController(ProdutoService produtoService, SessaoAtual sessaoAtual) {
		this.produtoService = produtoService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<ProdutoDTO.Response> listar(
			@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
		return PageResponseDTO.from(produtoService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}")
	public ProdutoDTO.Response buscar(@PathVariable Long id) {
		return produtoService.buscar(id, sessaoAtual.exigir());
	}

	@GetMapping("/{id}/historico-preco")
	public PageResponseDTO<PrecoCustoHistoricoDTO.Response> historico(
			@PathVariable Long id,
			@PageableDefault(size = 20, sort = "vigenciaIni", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(produtoService.listarHistorico(id, pageable, sessaoAtual.exigir()));
	}

	@PostMapping
	public ResponseEntity<ProdutoDTO.Response> criar(@Valid @RequestBody ProdutoDTO.Create dto) {
		ProdutoDTO.Response corpo = produtoService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/produtos/" + corpo.id()), corpo);
	}

	@PutMapping("/{id}")
	public ProdutoDTO.Response atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoDTO.Update dto) {
		return produtoService.atualizar(id, dto, sessaoAtual.exigir());
	}
}
