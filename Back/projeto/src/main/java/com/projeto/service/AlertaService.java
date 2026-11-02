package com.projeto.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.AlertaDTO;
import com.projeto.repository.AlertaRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;

@Service
@Transactional(readOnly = true)
public class AlertaService {

	private final AlertaRepository alertaRepository;

	public AlertaService(AlertaRepository alertaRepository) {
		this.alertaRepository = alertaRepository;
	}

	public Page<AlertaDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirLeituraAnalise(sessao);
		return alertaRepository.findByProdutoEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(AlertaDTO.Response::from);
	}
}
