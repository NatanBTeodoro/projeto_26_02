package com.projeto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findAllByOrderByNomeAsc();
}
