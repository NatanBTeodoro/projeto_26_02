package com.projeto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Estabelecimento;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
}
