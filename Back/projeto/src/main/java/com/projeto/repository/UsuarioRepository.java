package com.projeto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	Optional<Usuario> findByTokenAcesso(String tokenAcesso);

	boolean existsByEmailIgnoreCase(String email);

	List<Usuario> findAllByOrderByEmailAsc();
}
