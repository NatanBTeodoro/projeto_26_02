package com.projeto.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.dto.UnidadeDTO;
import com.projeto.entity.Estabelecimento;
import com.projeto.entity.Unidade;
import com.projeto.repository.EstabelecimentoRepository;
import com.projeto.repository.UnidadeRepository;
import com.projeto.seguranca.Acesso;
import com.projeto.seguranca.UsuarioAtual;
import com.projeto.util.exception.RecursoNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class UnidadeService {

	private final UnidadeRepository unidadeRepository;
	private final EstabelecimentoRepository estabelecimentoRepository;
	private final AuditoriaService auditoriaService;

	public UnidadeService(
			UnidadeRepository unidadeRepository,
			EstabelecimentoRepository estabelecimentoRepository,
			AuditoriaService auditoriaService) {
		this.unidadeRepository = unidadeRepository;
		this.estabelecimentoRepository = estabelecimentoRepository;
		this.auditoriaService = auditoriaService;
	}

	public Page<UnidadeDTO.Response> listar(Pageable pageable, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		return unidadeRepository.findByEstabelecimentoId(sessao.estabelecimentoId(), pageable)
				.map(UnidadeDTO.Response::from);
	}

	@Transactional
	public UnidadeDTO.Response criar(UnidadeDTO.Create dto, UsuarioAtual sessao) {
		Acesso.exigirEscritaCadastro(sessao);
		Acesso.exigirMesmoEstabelecimento(dto.estabelecimentoId(), sessao);
		Estabelecimento estabelecimento = estabelecimentoRepository.findById(sessao.estabelecimentoId())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento", sessao.estabelecimentoId()));
		Unidade unidade = new Unidade();
		unidade.setEstabelecimento(estabelecimento);
		unidade.setNome(dto.nome().trim());
		unidade.setAtiva(true);
		Unidade salva = unidadeRepository.save(unidade);
		auditoriaService.registrar(sessao, "CRIAR", "unidade", salva.getId(), null, salva.getNome());
		return UnidadeDTO.Response.from(salva);
	}
}
