package com.projeto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.PedidoItem;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
}
