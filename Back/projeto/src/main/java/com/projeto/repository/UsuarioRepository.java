package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	Page<Usuario> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Usuario> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);
}
