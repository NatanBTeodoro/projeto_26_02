package com.projeto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.PrecoCustoHistorico;

public interface PrecoCustoHistoricoRepository extends JpaRepository<PrecoCustoHistorico, Long> {

	Page<PrecoCustoHistorico> findByProdutoId(Long produtoId, Pageable pageable);
}
