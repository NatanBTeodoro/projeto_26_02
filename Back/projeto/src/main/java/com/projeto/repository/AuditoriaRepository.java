package com.projeto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

	Page<Auditoria> findByUsuarioEstabelecimentoId(Long estabelecimentoId, Pageable pageable);
}
