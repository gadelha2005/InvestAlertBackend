package com.investalert.investalert.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DashboardHistoricoPointResponseDTO {

    private LocalDateTime dataHora;
    private BigDecimal valorCarteira;
}
