package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.ConfigAnalise;

public interface ConfigAnaliseRepository extends JpaRepository<ConfigAnalise, Long> {

	Optional<ConfigAnalise> findByEstabelecimentoId(Long estabelecimentoId);
}
