package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.AnaliseExecucao;

public interface AnaliseExecucaoRepository extends JpaRepository<AnaliseExecucao, Long> {

	Page<AnaliseExecucao> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<AnaliseExecucao> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
