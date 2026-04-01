package com.investalert.investalert.dto.request;

import com.investalert.investalert.model.enums.TipoMovimentacaoMeta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class MetaMovimentacaoRequestDTO {

    @NotNull(message = "Tipo da movimentacao e obrigatorio")
    private TipoMovimentacaoMeta tipo;

    @NotNull(message = "Valor da movimentacao e obrigatorio")
    @DecimalMin(value = "0.01", message = "Valor da movimentacao deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
    private String descricao;

    private LocalDateTime dataMovimentacao;
}
