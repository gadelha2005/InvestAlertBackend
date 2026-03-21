package com.investalert.investalert.service;

import com.investalert.investalert.dto.response.DashboardResponseDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CarteiraService carteiraService;
    private final MetaService metaService;
    private final NotificacaoService notificacaoService;
    private final AlertaRepository alertaRepository;

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

        return DashboardResponseDTO.builder()
                .valorTotalCarteira(valorTotal)
                .lucroPrejuizoTotal(lucroPrejuizo)
                .variacaoPercentualTotal(variacaoPercentual)
                .alertasAtivos(alertasAtivos)
                .notificacoesNaoLidas(naoLidas)
                .ativos(todosAtivos)
                .metas(metas)
                .build();
    }
}