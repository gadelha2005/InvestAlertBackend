package com.investalert.investalert.dto.response;

import com.investalert.investalert.model.enums.CanalNotificacao;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificacaoResponseDTO {

    private Long id;
    private String mensagem;
    private CanalNotificacao canal;
    private Boolean lida;
    private LocalDateTime dataEnvio;
}