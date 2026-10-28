package com.projeto.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	Page<Pedido> findByEstabelecimentoId(Long estabelecimentoId, Pageable pageable);

	Optional<Pedido> findByIdAndEstabelecimentoId(Long id, Long estabelecimentoId);

	boolean existsByEstabelecimentoIdAndExternalId(Long estabelecimentoId, String externalId);
}
