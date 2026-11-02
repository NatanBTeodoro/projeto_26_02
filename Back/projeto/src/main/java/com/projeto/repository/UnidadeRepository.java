package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Unidade;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {

	Page<Unidade> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Unidade> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);

	java.util.List<Unidade> findByEstabelecimentoIdAndAtivaTrue(Long estabelecimentoId);
}
