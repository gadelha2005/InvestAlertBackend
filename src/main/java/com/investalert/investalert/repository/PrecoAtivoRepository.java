package com.investalert.investalert.repository;

import com.investalert.investalert.model.PrecoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrecoAtivoRepository extends JpaRepository<PrecoAtivo, Long> {

    Optional<PrecoAtivo> findTopByAtivoIdOrderByDataHoraDesc(Long ativoId);
}