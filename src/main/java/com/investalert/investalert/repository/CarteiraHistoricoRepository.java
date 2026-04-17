package com.investalert.investalert.repository;

import com.investalert.investalert.model.CarteiraHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarteiraHistoricoRepository extends JpaRepository<CarteiraHistorico, Long> {

    List<CarteiraHistorico> findByCarteiraIdOrderByDataHoraAsc(Long carteiraId);
}
