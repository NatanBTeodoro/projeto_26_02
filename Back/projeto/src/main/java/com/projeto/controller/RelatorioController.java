package com.projeto.controller;

import com.projeto.service.RelatorioService;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.service.AnaliseService;
import com.projeto.dto.RelatorioDTO;
import com.projeto.util.Exportador;
import com.projeto.seguranca.SessaoAtual;

@RestController
@RequestMapping("/api")
public class RelatorioController {

	private final RelatorioService relatorioService;
	private final AnaliseService analiseService;
	private final SessaoAtual sessaoAtual;

	public RelatorioController(RelatorioService relatorioService, AnaliseService analiseService, SessaoAtual sessaoAtual) {
		this.relatorioService = relatorioService;
		this.analiseService = analiseService;
		this.sessaoAtual = sessaoAtual;
	}

	@GetMapping("/dashboard")
	public RelatorioDTO.Dashboard dashboard(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		return relatorioService.dashboard(periodoIni, periodoFim, sessaoAtual.exigir());
	}

	@GetMapping("/relatorios/abc")
	public RelatorioDTO.Abc abc(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		return relatorioService.abc(periodoIni, periodoFim, sessaoAtual.exigir());
	}

	@GetMapping("/relatorios/clientes")
	public List<RelatorioDTO.Cliente> clientes(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		return relatorioService.clientes(periodoIni, periodoFim, sessaoAtual.exigir());
	}

	@GetMapping("/relatorios/unidades")
	public List<RelatorioDTO.UnidadeDesempenho> unidades(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		return relatorioService.unidades(periodoIni, periodoFim, sessaoAtual.exigir());
	}

	@GetMapping(value = "/relatorios/abc/exportar.csv", produces = "text/csv")
	public ResponseEntity<byte[]> abcCsv(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		byte[] corpo = Exportador.csvAbc(relatorioService.abc(periodoIni, periodoFim, sessaoAtual.exigir()));
		return arquivo(corpo, "abc.csv", "text/csv");
	}

	@GetMapping(value = "/relatorios/abc/exportar.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> abcPdf(@RequestParam Instant periodoIni, @RequestParam Instant periodoFim) {
		byte[] corpo = Exportador.pdfAbc(relatorioService.abc(periodoIni, periodoFim, sessaoAtual.exigir()));
		return arquivo(corpo, "abc.pdf", MediaType.APPLICATION_PDF_VALUE);
	}

	@GetMapping(value = "/analises/{id}/exportar.csv", produces = "text/csv")
	public ResponseEntity<byte[]> analiseCsv(@org.springframework.web.bind.annotation.PathVariable Long id) {
		byte[] corpo = analiseService.exportarCsv(id, sessaoAtual.exigir());
		return arquivo(corpo, "analise-" + id + ".csv", "text/csv");
	}

	@GetMapping(value = "/analises/{id}/exportar.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<byte[]> analisePdf(@org.springframework.web.bind.annotation.PathVariable Long id) {
		byte[] corpo = analiseService.exportarPdf(id, sessaoAtual.exigir());
		return arquivo(corpo, "analise-" + id + ".pdf", MediaType.APPLICATION_PDF_VALUE);
	}

	private ResponseEntity<byte[]> arquivo(byte[] corpo, String nome, String tipo) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
				.contentType(MediaType.parseMediaType(tipo))
				.body(corpo);
	}
}
