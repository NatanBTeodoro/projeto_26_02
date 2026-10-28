package com.projeto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.FichaTecnicaItem;

public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Long> {

	List<FichaTecnicaItem> findByProdutoId(Long produtoId);

	boolean existsByProdutoIdAndInsumoId(Long produtoId, Long insumoId);
}
