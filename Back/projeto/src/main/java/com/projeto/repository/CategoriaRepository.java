package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	Page<Categoria> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Categoria> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
