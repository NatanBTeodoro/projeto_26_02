package com.projeto.controller;

import com.projeto.service.AlertaService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.AlertaDTO;
import com.projeto.dto.PageResponseDTO;
import com.projeto.seguranca.SessaoAtual;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

	private final AlertaService alertaService;
	private final SessaoAtual sessaoAtual;

	public AlertaController(AlertaService alertaService, SessaoAtual sessaoAtual) {
		this.alertaService = alertaService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping
	public PageResponseDTO<AlertaDTO.Response> listar(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return PageResponseDTO.from(alertaService.listar(pageable, sessaoAtual.exigir()));
	}
}
