package com.investalert.investalert.repository;

import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.enums.TipoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    Optional<Ativo> findByTicker(String ticker);

    boolean existsByTicker(String ticker);

    List<Ativo> findByTipo(TipoAtivo tipo);
}