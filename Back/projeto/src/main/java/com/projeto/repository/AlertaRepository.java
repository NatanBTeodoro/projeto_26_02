package com.projeto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

	Page<Alerta> findByProdutoEstabelecimentoId(Long estabelecimentoId, Pageable pageable);
}
