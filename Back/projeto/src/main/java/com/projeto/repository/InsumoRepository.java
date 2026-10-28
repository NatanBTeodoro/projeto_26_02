package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Insumo;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

	Page<Insumo> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Insumo> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
