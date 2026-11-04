package com.projeto.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.dto.AnaliseDTO;
import com.projeto.service.AnaliseService;

@RestController
@RequestMapping("/api/analise")
public class AnaliseController {

	private final AnaliseService analiseService;

	public AnaliseController(AnaliseService analiseService) {
		this.analiseService = analiseService;
	}

	@GetMapping
	public AnaliseDTO.Response consultar(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
		return analiseService.consultar(de, ate);
	}
}
