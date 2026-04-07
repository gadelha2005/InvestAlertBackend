package com.investalert.investalert.service;

import com.investalert.investalert.dto.response.DashboardResponseDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.DashboardHistoricoPointResponseDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.model.CarteiraAtivo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.repository.AlertaRepository;
import com.investalert.investalert.repository.CarteiraAtivoRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CarteiraService carteiraService;
    private final MetaService metaService;
    private final NotificacaoService notificacaoService;
    private final AlertaRepository alertaRepository;
    private final CarteiraAtivoRepository carteiraAtivoRepository;
    private final PrecoAtivoRepository precoAtivoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard(Long usuarioId) {
        List<CarteiraAtivoResponseDTO> todosAtivos = carteiraService
                .listarPorUsuario(usuarioId).stream()
                .flatMap(c -> c.getAtivos().stream())
                .toList();

        BigDecimal valorTotal = todosAtivos.stream()
                .filter(a -> a.getValorAtual() != null)
                .map(CarteiraAtivoResponseDTO::getValorAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorInvestido = todosAtivos.stream()
                .map(CarteiraAtivoResponseDTO::getValorInvestido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucroPrejuizo = valorTotal.subtract(valorInvestido);

        BigDecimal variacaoPercentual = null;
        if (valorInvestido.compareTo(BigDecimal.ZERO) != 0) {
            variacaoPercentual = lucroPrejuizo
                    .divide(valorInvestido, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<MetaResponseDTO> metas = metaService.listarPorUsuario(usuarioId);
        long alertasAtivos = alertaRepository.findByUsuarioId(usuarioId).stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivado()))
                .count();
        long naoLidas = notificacaoService.contarNaoLidas(usuarioId);
        List<DashboardHistoricoPointResponseDTO> historicoCarteira = montarHistoricoCarteira(usuarioId);

        return DashboardResponseDTO.builder()
                .valorTotalCarteira(valorTotal)
                .lucroPrejuizoTotal(lucroPrejuizo)
                .variacaoPercentualTotal(variacaoPercentual)
                .alertasAtivos(alertasAtivos)
                .notificacoesNaoLidas(naoLidas)
                .ativos(todosAtivos)
                .historicoCarteira(historicoCarteira)
                .metas(metas)
                .build();
    }

    private List<DashboardHistoricoPointResponseDTO> montarHistoricoCarteira(Long usuarioId) {
        List<CarteiraAtivo> posicoes = carteiraAtivoRepository.findByUsuarioIdWithAtivo(usuarioId);
        if (posicoes.isEmpty()) {
            return List.of();
        }

        List<Long> ativoIds = posicoes.stream()
                .map(carteiraAtivo -> carteiraAtivo.getAtivo().getId())
                .distinct()
                .toList();

        Map<Long, List<PrecoAtivo>> precosPorAtivo = precoAtivoRepository
                .findByAtivoIdInOrderByDataHoraAsc(ativoIds)
                .stream()
                .collect(Collectors.groupingBy(preco -> preco.getAtivo().getId()));

        List<LocalDateTime> marcos = new ArrayList<>();
        posicoes.stream()
                .map(CarteiraAtivo::getDataCompra)
                .filter(dataCompra -> dataCompra != null)
                .forEach(marcos::add);

        precosPorAtivo.values().stream()
                .flatMap(List::stream)
                .map(PrecoAtivo::getDataHora)
                .filter(dataHora -> dataHora != null)
                .forEach(marcos::add);

        List<LocalDateTime> marcosOrdenados = marcos.stream()
                .distinct()
                .sorted()
                .toList();

        if (marcosOrdenados.isEmpty()) {
            return List.of();
        }

        Map<Long, BigDecimal> ultimoPrecoConhecidoPorAtivo = new HashMap<>();
        List<DashboardHistoricoPointResponseDTO> historico = new ArrayList<>();

        for (LocalDateTime marco : marcosOrdenados) {
            BigDecimal valorCarteiraNoMarco = BigDecimal.ZERO;

            for (CarteiraAtivo posicao : posicoes) {
                if (posicao.getDataCompra() == null || posicao.getDataCompra().isAfter(marco)) {
                    continue;
                }

                Long ativoId = posicao.getAtivo().getId();
                BigDecimal precoNoMarco = buscarPrecoNoMarco(
                        precosPorAtivo.getOrDefault(ativoId, List.of()),
                        marco,
                        ultimoPrecoConhecidoPorAtivo.get(ativoId)
                );

                if (precoNoMarco == null) {
                    precoNoMarco = posicao.getPrecoMedio();
                }

                ultimoPrecoConhecidoPorAtivo.put(ativoId, precoNoMarco);
                valorCarteiraNoMarco = valorCarteiraNoMarco.add(
                        precoNoMarco.multiply(posicao.getQuantidade())
                );
            }

            historico.add(DashboardHistoricoPointResponseDTO.builder()
                    .dataHora(marco)
                    .valorCarteira(valorCarteiraNoMarco)
                    .build());
        }

        return historico.stream()
                .sorted(Comparator.comparing(DashboardHistoricoPointResponseDTO::getDataHora))
                .toList();
    }

    private BigDecimal buscarPrecoNoMarco(List<PrecoAtivo> precos, LocalDateTime marco, BigDecimal fallback) {
        BigDecimal ultimoPreco = fallback;

        for (PrecoAtivo preco : precos) {
            if (preco.getDataHora() == null || preco.getDataHora().isAfter(marco)) {
                break;
            }

            ultimoPreco = preco.getPreco();
        }

        return ultimoPreco;
    }
}
