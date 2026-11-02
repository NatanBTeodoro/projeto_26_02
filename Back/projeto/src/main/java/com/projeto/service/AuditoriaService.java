package com.projeto.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.AuditoriaDTO;
import com.projeto.entity.Auditoria;
import com.projeto.repository.AuditoriaRepository;
import com.projeto.repository.UsuarioRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;

@Service
@Transactional(readOnly = true)
public class AuditoriaService {

	private final AuditoriaRepository auditoriaRepository;
	private final UsuarioRepository usuarioRepository;

	public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
		this.auditoriaRepository = auditoriaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public Page<AuditoriaDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirAdministrador(sessao);
		return auditoriaRepository.findByUsuarioEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(AuditoriaDTO.Response::from);
	}

	@Transactional
	public void registrar(UsuarioAtual sessao, String acao, String entidade, Long entidadeId, String anterior, String novo) {
		Auditoria auditoria = new Auditoria();
		if (sessao != null && sessao.id() != null) {
			usuarioRepository.findById(sessao.id()).ifPresent(auditoria::setUsuario);
		}
		auditoria.setAcao(acao);
		auditoria.setEntidade(entidade);
		auditoria.setEntidadeId(entidadeId);
		auditoria.setValorAnterior(limitar(anterior));
		auditoria.setValorNovo(limitar(novo));
		auditoria.setDataHora(Instant.now());
		auditoriaRepository.save(auditoria);
	}

	private String limitar(String texto) {
		if (texto == null || texto.length() <= 4000) {
			return texto;
		}
		return texto.substring(0, 4000);
	}
}
