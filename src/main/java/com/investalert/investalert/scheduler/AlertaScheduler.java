package com.investalert.investalert.scheduler;

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
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaScheduler {

    private final AlertaRepository alertaRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final NotificacaoRepository notificacaoRepository;

    // Executa após atualização de preços — a cada 5 minutos com 30s de delay
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

        if (condicaoAtingida) {
            dispararNotificacao(alerta, preco);
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
        // Para variação, o valor_alvo representa o percentual mínimo de variação
        // Busca o preço de 24h atrás para calcular a variação
        List<PrecoAtivo> historico = precoAtivoRepository
                .findTopByAtivoIdOrderByDataHoraDesc(alerta.getAtivo().getId())
                .map(List::of)
                .orElse(List.of());

        if (historico.isEmpty()) {
            return false;
        }

        // Simplificado: compara com o valor_alvo como percentual absoluto de variação
        BigDecimal variacaoAbsoluta = precoAtual.subtract(alerta.getValorAlvo())
                .abs()
                .divide(alerta.getValorAlvo(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return variacaoAbsoluta.compareTo(alerta.getValorAlvo()) >= 0;
    }

    private void dispararNotificacao(Alerta alerta, BigDecimal precoAtual) {
        String mensagem = construirMensagem(alerta, precoAtual);

        Notificacao notificacao = Notificacao.builder()
                .usuario(alerta.getUsuario())
                .alerta(alerta)
                .mensagem(mensagem)
                .canal(CanalNotificacao.INTERNO)
                .build();

        notificacaoRepository.save(notificacao);

        log.info("Notificação criada para alerta {} — {}: R$ {}",
                alerta.getId(), alerta.getAtivo().getTicker(), precoAtual);
    }

    private String construirMensagem(Alerta alerta, BigDecimal precoAtual) {
        String ticker = alerta.getAtivo().getTicker();
        String tipo = switch (alerta.getTipo()) {
            case PRECO_ACIMA -> "atingiu o preço máximo de R$ " + alerta.getValorAlvo();
            case PRECO_ABAIXO -> "atingiu o preço mínimo de R$ " + alerta.getValorAlvo();
            case VARIACAO -> "teve variação de " + alerta.getValorAlvo() + "%";
        };

        return String.format("⚠️ Alerta: %s %s. Preço atual: R$ %s", ticker, tipo, precoAtual);
    }
}