package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Alerta;
import com.projeto.entity.enums.TipoAlerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

	Page<Alerta> findByProdutoEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	long countByProdutoEstabelecimentoIdAndAtivoTrue(Long estabelecimentoId);

	Optional<Alerta> findByProdutoIdAndTipo(Long produtoId, TipoAlerta tipo);
}
