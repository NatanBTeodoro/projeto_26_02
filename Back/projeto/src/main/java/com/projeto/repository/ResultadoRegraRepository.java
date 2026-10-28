package com.projeto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.ResultadoRegra;

public interface ResultadoRegraRepository extends JpaRepository<ResultadoRegra, Long> {

	Page<ResultadoRegra> findByExecucaoId(Long execucaoId, Pageable pageable);
}
