package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Importacao;

public interface ImportacaoRepository extends JpaRepository<Importacao, Long> {

	Page<Importacao> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Importacao> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
