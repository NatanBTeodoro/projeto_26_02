package com.projeto.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.service.AuditoriaService;
import com.projeto.dto.ConfigAnaliseDTO;
import com.projeto.entity.ConfigAnalise;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.enums.CriterioPopularidade;
import com.projeto.repository.ConfigAnaliseRepository;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@Service
public class ConfigAnaliseService {

	private final ConfigAnaliseRepository configRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final AuditoriaService auditoriaService;

	public ConfigAnaliseService(
			ConfigAnaliseRepository configRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			AuditoriaService auditoriaService) {
		this.configRepository = configRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.auditoriaService = auditoriaService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ConfigAnalise obterOuCriar(Long estabelecimentoId) {
		return configRepository.findByEstabelecimentoId(estabelecimentoId).orElseGet(() -> {
			Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
					.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", estabelecimentoId));
			ConfigAnalise config = new ConfigAnalise();
			config.setEstabelecimento(estabelecimento);
			config.setSuporteMin(new BigDecimal("0.0200"));
			config.setConfiancaMin(new BigDecimal("0.3000"));
			config.setLiftMin(new BigDecimal("1.0000"));
			config.setPeriodoMinimoDias(30);
			config.setNAlertaCao(3);
			config.setCriterioPopularidade(CriterioPopularidade.PONDERADA);
			config.setEscopoPorCategoria(true);
			return configRepository.save(config);
		});
	}

	@Transactional
	public ConfigAnaliseDTO.Response buscar(UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		return ConfigAnaliseDTO.Response.from(obterOuCriar(sessao.estabelecimentoId()));
	}

	@Transactional
	public ConfigAnaliseDTO.Response atualizar(ConfigAnaliseDTO.Update dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaAnalise(sessao);
		ConfigAnalise config = obterOuCriar(sessao.estabelecimentoId());
		String anterior = config.getSuporteMin() + "/" + config.getConfiancaMin() + "/" + config.getLiftMin();
		config.setSuporteMin(dto.suporteMin());
		config.setConfiancaMin(dto.confiancaMin());
		config.setLiftMin(dto.liftMin());
		config.setPeriodoMinimoDias(dto.periodoMinimoDias());
		config.setNAlertaCao(dto.nAlertaCao());
		config.setCriterioPopularidade(dto.criterioPopularidade());
		config.setEscopoPorCategoria(dto.escopoPorCategoria());
		ConfigAnalise salvo = configRepository.save(config);
		auditoriaService.registrar(sessao, "ATUALIZAR", "config_analise", salvo.getId(), anterior,
				salvo.getSuporteMin() + "/" + salvo.getConfiancaMin() + "/" + salvo.getLiftMin());
		return ConfigAnaliseDTO.Response.from(salvo);
	}
}
