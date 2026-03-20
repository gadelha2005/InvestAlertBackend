package com.investalert.investalert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class DashboardResponseDTO {

    private BigDecimal valorTotalCarteira;
    private BigDecimal lucroPrejuizoTotal;
    private BigDecimal variacaoPercentualTotal;
    private Long alertasAtivos;
    private Long notificacoesNaoLidas;
    private List<CarteiraAtivoResponseDTO> ativos;
    private List<MetaResponseDTO> metas;
}