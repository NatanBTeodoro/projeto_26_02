package com.projeto.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findAllByOrderByDataHoraDesc();

	List<Pedido> findByDataHoraGreaterThanEqualAndDataHoraLessThan(Instant inicio, Instant fim);
}
