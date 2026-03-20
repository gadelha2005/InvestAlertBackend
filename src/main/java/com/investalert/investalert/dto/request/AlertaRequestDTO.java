package com.investalert.investalert.dto.request;

import com.investalert.investalert.model.enums.TipoAlerta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AlertaRequestDTO {

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @NotNull(message = "Tipo é obrigatório")
    private TipoAlerta tipo;

    @NotNull(message = "Valor alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor alvo deve ser maior que zero")
    private BigDecimal valorAlvo;

    private Boolean notificarWhatsapp = false;
}