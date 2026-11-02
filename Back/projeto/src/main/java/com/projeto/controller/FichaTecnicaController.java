package com.projeto.controller;

import com.projeto.service.FichaTecnicaService;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.FichaTecnicaItemDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;
import com.projeto.util.HttpRespostas;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos/{produtoId}/ficha-tecnica")
public class FichaTecnicaController {

	private final FichaTecnicaService fichaTecnicaService;
	private final SessaoAtual sessaoAtual;

	public FichaTecnicaController(FichaTecnicaService fichaTecnicaService, SessaoAtual sessaoAtual) {
		this.fichaTecnicaService = fichaTecnicaService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<FichaTecnicaItemDTO.Response> listar(
			@PathVariable Long produtoId,
			@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return PageResponseDTO.from(fichaTecnicaService.listar(produtoId, pageable, sessaoAtual.exigir()));
	}

	@PostMapping
	public ResponseEntity<FichaTecnicaItemDTO.Response> criar(
			@PathVariable Long produtoId,
			@Valid @RequestBody FichaTecnicaItemDTO.Create dto) {
		FichaTecnicaItemDTO.Response corpo = fichaTecnicaService.criar(produtoId, dto, sessaoAtual.exigir());
		return HttpRespostas.created(URI.create("/api/produtos/" + produtoId + "/ficha-tecnica/" + corpo.id()), corpo);
	}
}
