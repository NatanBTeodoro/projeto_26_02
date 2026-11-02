package com.projeto.controller;

import com.projeto.service.AuditoriaService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.AuditoriaDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

	private final AuditoriaService auditoriaService;
	private final SessaoAtual sessaoAtual;

	public AuditoriaController(AuditoriaService auditoriaService, SessaoAtual sessaoAtual) {
		this.auditoriaService = auditoriaService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<AuditoriaDTO.Response> listar(
			@PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(auditoriaService.listar(pageable, sessaoAtual.exigir()));
	}
}
