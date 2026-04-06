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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaScheduler {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

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
        List<PrecoAtivo> precosRecentes = precoAtivoRepository
                .findTop2ByAtivoIdOrderByDataHoraDesc(alerta.getAtivo().getId());

        if (precosRecentes.isEmpty()) {
            log.debug("Sem preco disponivel para o ativo: {}", alerta.getAtivo().getTicker());
            return;
        }

        BigDecimal precoAtual = precosRecentes.get(0).getPreco();
        BigDecimal precoAnterior = precosRecentes.size() > 1 ? precosRecentes.get(1).getPreco() : null;
        boolean condicaoAtingida = verificarCondicao(alerta, precoAtual, precoAnterior);

        if (condicaoAtingida && !Boolean.TRUE.equals(alerta.getCondicaoDisparada())) {
            dispararNotificacoes(alerta, precoAtual, precoAnterior);
            alerta.setCondicaoDisparada(true);
        } else if (!condicaoAtingida && Boolean.TRUE.equals(alerta.getCondicaoDisparada())) {
            alerta.setCondicaoDisparada(false);
        }
    }

    private boolean verificarCondicao(Alerta alerta, BigDecimal precoAtual, BigDecimal precoAnterior) {
        return switch (alerta.getTipo()) {
            case PRECO_ACIMA -> precoAtual.compareTo(alerta.getValorAlvo()) >= 0;
            case PRECO_ABAIXO -> precoAtual.compareTo(alerta.getValorAlvo()) <= 0;
            case VARIACAO -> verificarVariacao(alerta, precoAtual, precoAnterior);
        };
    }

    private boolean verificarVariacao(Alerta alerta, BigDecimal precoAtual, BigDecimal precoAnterior) {
        if (precoAnterior == null || precoAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        BigDecimal variacaoPercentual = calcularVariacaoPercentual(precoAtual, precoAnterior);
        return variacaoPercentual.compareTo(alerta.getValorAlvo()) >= 0;
    }

    private BigDecimal calcularVariacaoPercentual(BigDecimal precoAtual, BigDecimal precoAnterior) {
        return precoAtual
                .subtract(precoAnterior)
                .abs()
                .divide(precoAnterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void dispararNotificacoes(Alerta alerta, BigDecimal precoAtual, BigDecimal precoAnterior) {
        String mensagem = construirMensagem(alerta, precoAtual, precoAnterior);
        String assunto = "InvestAlert - Alerta disparado: " + alerta.getAtivo().getTicker();

        salvarNotificacaoInterna(alerta, mensagem);

        emailService.enviar(
                alerta.getUsuario().getEmail(),
                assunto,
                mensagem
        );

        log.info("Notificacoes disparadas para alerta {} - {}: {}",
                alerta.getId(), alerta.getAtivo().getTicker(), formatarMoeda(precoAtual));
    }

    private void salvarNotificacaoInterna(Alerta alerta, String mensagem) {
        Notificacao notificacao = Notificacao.builder()
                .usuario(alerta.getUsuario())
                .alerta(alerta)
                .mensagem(mensagem)
                .canal(CanalNotificacao.INTERNO)
                .build();

        notificacaoRepository.save(notificacao);
        log.debug("Notificacao interna salva para alerta {}", alerta.getId());
    }

    private String construirMensagem(Alerta alerta, BigDecimal precoAtual, BigDecimal precoAnterior) {
        String ticker = alerta.getAtivo().getTicker();
        String condicao = switch (alerta.getTipo()) {
            case PRECO_ACIMA -> String.format(
                    "o ativo atingiu ou ultrapassou o preco maximo configurado de %s",
                    formatarMoeda(alerta.getValorAlvo())
            );
            case PRECO_ABAIXO -> String.format(
                    "o ativo atingiu ou ficou abaixo do preco minimo configurado de %s",
                    formatarMoeda(alerta.getValorAlvo())
            );
            case VARIACAO -> construirDescricaoVariacao(alerta, precoAtual, precoAnterior);
        };

        return String.format(
                "Alerta InvestAlert\n\n" +
                "Ativo: %s\n" +
                "Condicao: %s\n" +
                "Preco atual: %s\n\n" +
                "Acesse sua carteira para mais detalhes.",
                ticker, condicao, formatarMoeda(precoAtual)
        );
    }

    private String construirDescricaoVariacao(Alerta alerta, BigDecimal precoAtual, BigDecimal precoAnterior) {
        if (precoAnterior == null || precoAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return String.format(
                    "o ativo atingiu a variacao configurada de %s",
                    formatarPercentual(alerta.getValorAlvo())
            );
        }

        BigDecimal diferenca = precoAtual.subtract(precoAnterior);
        BigDecimal variacaoPercentual = calcularVariacaoPercentual(precoAtual, precoAnterior);
        String direcao = diferenca.compareTo(BigDecimal.ZERO) >= 0 ? "para cima" : "para baixo";

        return String.format(
                "o ativo teve variacao %s de %s em relacao ao preco anterior de %s, superando o limite configurado de %s",
                direcao,
                formatarPercentual(variacaoPercentual),
                formatarMoeda(precoAnterior),
                formatarPercentual(alerta.getValorAlvo())
        );
    }

    private String formatarMoeda(BigDecimal valor) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        return formatador.format(valor);
    }

    private String formatarPercentual(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }
}
