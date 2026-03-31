package com.investalert.investalert.scheduler;

import com.investalert.investalert.integration.EmailService;
import com.investalert.investalert.model.Alerta;
import com.investalert.investalert.model.Notificacao;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.enums.CanalNotificacao;
import com.investalert.investalert.repository.AlertaRepository;
import com.investalert.investalert.repository.NotificacaoRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaScheduler {

    private final AlertaRepository alertaRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final EmailService emailService;

    @Scheduled(fixedRateString = "${scheduler.alerta.intervalo:300000}",
               initialDelayString = "${scheduler.alerta.delay:30000}")
    @Transactional
    public void verificarAlertas() {
        List<Alerta> alertasAtivos = alertaRepository.findAllAtivosWithAtivo();

        if (alertasAtivos.isEmpty()) {
            log.debug("Nenhum alerta ativo para verificar.");
            return;
        }

        log.info("Verificando {} alertas ativos.", alertasAtivos.size());

        for (Alerta alerta : alertasAtivos) {
            verificarAlerta(alerta);
        }
    }

    private void verificarAlerta(Alerta alerta) {
        Optional<PrecoAtivo> precoAtual = precoAtivoRepository
                .findTopByAtivoIdOrderByDataHoraDesc(alerta.getAtivo().getId());

        if (precoAtual.isEmpty()) {
            log.debug("Sem preço disponível para o ativo: {}", alerta.getAtivo().getTicker());
            return;
        }

        BigDecimal preco = precoAtual.get().getPreco();
        boolean condicaoAtingida = verificarCondicao(alerta, preco);

        if (condicaoAtingida && !Boolean.TRUE.equals(alerta.getCondicaoDisparada())) {
            dispararNotificacoes(alerta, preco);
            alerta.setCondicaoDisparada(true);
        } else if (!condicaoAtingida && Boolean.TRUE.equals(alerta.getCondicaoDisparada())) {
            alerta.setCondicaoDisparada(false);
        }
    }

    private boolean verificarCondicao(Alerta alerta, BigDecimal precoAtual) {
        return switch (alerta.getTipo()) {
            case PRECO_ACIMA -> precoAtual.compareTo(alerta.getValorAlvo()) >= 0;
            case PRECO_ABAIXO -> precoAtual.compareTo(alerta.getValorAlvo()) <= 0;
            case VARIACAO -> verificarVariacao(alerta, precoAtual);
        };
    }

    private boolean verificarVariacao(Alerta alerta, BigDecimal precoAtual) {
        if (alerta.getValorAlvo().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal variacaoPercentual = precoAtual
                .subtract(alerta.getValorAlvo())
                .abs()
                .divide(alerta.getValorAlvo(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return variacaoPercentual.compareTo(alerta.getValorAlvo()) >= 0;
    }

    private void dispararNotificacoes(Alerta alerta, BigDecimal precoAtual) {
        String mensagem = construirMensagem(alerta, precoAtual);
        String assunto = "InvestAlert — Alerta disparado: " + alerta.getAtivo().getTicker();

        // Notificação interna — sempre
        salvarNotificacaoInterna(alerta, mensagem);

        // Email — sempre
        emailService.enviar(
                alerta.getUsuario().getEmail(),
                assunto,
                mensagem
        );

        log.info("Notificações disparadas para alerta {} — {}: R$ {}",
                alerta.getId(), alerta.getAtivo().getTicker(), precoAtual);
    }

    private void salvarNotificacaoInterna(Alerta alerta, String mensagem) {
        Notificacao notificacao = Notificacao.builder()
                .usuario(alerta.getUsuario())
                .alerta(alerta)
                .mensagem(mensagem)
                .canal(CanalNotificacao.INTERNO)
                .build();

        notificacaoRepository.save(notificacao);
        log.debug("Notificação interna salva para alerta {}", alerta.getId());
    }

    private String construirMensagem(Alerta alerta, BigDecimal precoAtual) {
        String ticker = alerta.getAtivo().getTicker();
        String condicao = switch (alerta.getTipo()) {
            case PRECO_ACIMA -> "atingiu o preço máximo de R$ " + alerta.getValorAlvo();
            case PRECO_ABAIXO -> "atingiu o preço mínimo de R$ " + alerta.getValorAlvo();
            case VARIACAO -> "teve variação de " + alerta.getValorAlvo() + "%";
        };

        return String.format(
                "Alerta InvestAlert\n\n" +
                "Ativo: %s\n" +
                "Condição: %s\n" +
                "Preço atual: R$ %s\n\n" +
                "Acesse sua carteira para mais detalhes.",
                ticker, condicao, precoAtual
        );
    }
}