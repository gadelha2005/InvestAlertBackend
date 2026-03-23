package com.investalert.investalert.scheduler;

import com.investalert.investalert.integration.PrecoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrecoScheduler {

    private final PrecoService precoService;

    //Intervalo 5 minutos
    @Scheduled(fixedRateString = "${scheduler.preco.intervalo:300000}")
    public void atualizarPrecos() {
        log.info("Scheduler de preços iniciado.");
        precoService.atualizarTodosOsPrecos();
        log.info("Scheduler de preços finalizado.");
    }
}