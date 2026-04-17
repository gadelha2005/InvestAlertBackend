package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.TipoEventoCarteira;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CarteiraHistoricoResponseDTO {

    private Long id;
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private TipoEventoCarteira tipoEvento;
    private String descricao;
}
