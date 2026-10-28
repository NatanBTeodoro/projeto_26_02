package com.projeto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.ResultadoClassificacao;

public interface ResultadoClassificacaoRepository extends JpaRepository<ResultadoClassificacao, Long> {

	Page<ResultadoClassificacao> findByExecucaoId(Long execucaoId, Pageable pageable);
}
