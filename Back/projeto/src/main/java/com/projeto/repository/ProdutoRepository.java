package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	Page<Produto> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Produto> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
