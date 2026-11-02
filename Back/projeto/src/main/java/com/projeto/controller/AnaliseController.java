package com.projeto.controller;

import com.projeto.service.AnaliseService;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.AnaliseDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/analises")
public class AnaliseController {

	private final AnaliseService analiseService;
	private final SessaoAtual sessaoAtual;

	public AnaliseController(AnaliseService analiseService, SessaoAtual sessaoAtual) {
		this.analiseService = analiseService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<AnaliseDTO.ExecucaoResponse> listar(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(analiseService.listar(pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/comparar")
	public AnaliseDTO.Comparacao comparar(
			@RequestParam Instant periodoIniA,
			@RequestParam Instant periodoFimA,
			@RequestParam Instant periodoIniB,
			@RequestParam Instant periodoFimB) {
		return analiseService.comparar(periodoIniA, periodoFimA, periodoIniB, periodoFimB, sessaoAtual.exigir());
	}

	@GetMapping("/{id}")
	public AnaliseDTO.ExecucaoResponse buscar(@PathVariable Long id) {
		return analiseService.buscar(id, sessaoAtual.exigir());
	}

	@GetMapping("/{id}/classificacoes")
	public PageResponseDTO<AnaliseDTO.ClassificacaoResponse> classificacoes(
			@PathVariable Long id,
			@PageableDefault(size = 20, sort = "classe") Pageable pageable) {
		return PageResponseDTO.from(analiseService.classificacoes(id, pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}/regras")
	public PageResponseDTO<AnaliseDTO.RegraResponse> regras(
			@PathVariable Long id,
			@PageableDefault(size = 20, sort = "lift", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(analiseService.regras(id, pageable, sessaoAtual.exigir()));
	}

	@GetMapping("/{id}/combos")
	public List<AnaliseDTO.Combo> combos(@PathVariable Long id) {
		return analiseService.combos(id, sessaoAtual.exigir());
	}

	@PostMapping
	public ResponseEntity<AnaliseDTO.ExecucaoResponse> criar(@Valid @RequestBody AnaliseDTO.Create dto) {
		AnaliseDTO.ExecucaoResponse corpo = analiseService.criar(dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/analises/" + corpo.id()), corpo);
	}

	@PostMapping("/simular")
	public AnaliseDTO.SimulacaoResponse simular(@Valid @RequestBody AnaliseDTO.Simulacao dto) {
		return analiseService.simular(dto, sessaoAtual.exigir());
	}

	@PostMapping("/{id}/reexecutar")
	public AnaliseDTO.ExecucaoResponse reexecutar(@PathVariable Long id) {
		return analiseService.reexecutar(id, sessaoAtual.exigir());
	}
}
