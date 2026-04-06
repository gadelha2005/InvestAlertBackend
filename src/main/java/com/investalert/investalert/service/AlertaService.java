package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.AlertaRequestDTO;
import com.investalert.investalert.dto.response.AlertaResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Alerta;
import com.investalert.investalert.model.Ativo;
import com.investalert.investalert.model.PrecoAtivo;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoAlerta;
import com.investalert.investalert.repository.AlertaRepository;
import com.investalert.investalert.repository.PrecoAtivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final PrecoAtivoRepository precoAtivoRepository;
    private final AtivoService ativoService;
    private final UsuarioService usuarioService;

    @Transactional
    public AlertaResponseDTO criar(Long usuarioId, AlertaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorEmail(
                usuarioService.buscarPorId(usuarioId).getEmail()
        );

        Ativo ativo = ativoService.buscarEntidadePorTicker(dto.getTicker());
        BigDecimal precoAtual = buscarPrecoAtual(ativo.getId());

        validarValorAlvo(dto, ativo, precoAtual);

        Alerta alerta = Alerta.builder()
                .usuario(usuario)
                .ativo(ativo)
                .tipo(dto.getTipo())
                .valorAlvo(dto.getValorAlvo())
                .notificarWhatsapp(dto.getNotificarWhatsapp())
                .build();

        Alerta salvo = alertaRepository.save(alerta);

        return toResponse(salvo, precoAtual);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> listarPorUsuario(Long usuarioId) {
        return alertaRepository.findByUsuarioId(usuarioId).stream()
                .map(alerta -> toResponse(alerta, buscarPrecoAtual(alerta.getAtivo().getId())))
                .toList();
    }

    @Transactional
    public void deletar(Long alertaId, Long usuarioId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertaId));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para deletar este alerta");
        }

        alertaRepository.delete(alerta);
    }

    @Transactional
    public AlertaResponseDTO alterarStatus(Long alertaId, Long usuarioId, Boolean ativado) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertaId));

        if (!alerta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para alterar este alerta");
        }

        alerta.setAtivado(ativado);
        if (Boolean.FALSE.equals(ativado)) {
            alerta.setCondicaoDisparada(false);
        }
        BigDecimal precoAtual = buscarPrecoAtual(alerta.getAtivo().getId());

        return toResponse(alertaRepository.save(alerta), precoAtual);
    }

    private BigDecimal buscarPrecoAtual(Long ativoId) {
        return precoAtivoRepository.findTopByAtivoIdOrderByDataHoraDesc(ativoId)
                .map(PrecoAtivo::getPreco)
                .orElse(null);
    }

    private void validarValorAlvo(AlertaRequestDTO dto, Ativo ativo, BigDecimal precoAtual) {
        if (dto.getTipo() != TipoAlerta.PRECO_ACIMA && dto.getTipo() != TipoAlerta.PRECO_ABAIXO) {
            return;
        }

        if (precoAtual == null) {
            throw new BusinessException("Nao foi possivel validar o alerta porque o ativo "
                    + ativo.getTicker() + " ainda nao possui preco atual.");
        }

        if (dto.getTipo() == TipoAlerta.PRECO_ACIMA && dto.getValorAlvo().compareTo(precoAtual) < 0) {
            throw new BusinessException("Para alerta de preco maximo, o valor alvo deve ser maior ou igual ao preco atual de "
                    + precoAtual + ".");
        }

        if (dto.getTipo() == TipoAlerta.PRECO_ABAIXO && dto.getValorAlvo().compareTo(precoAtual) > 0) {
            throw new BusinessException("Para alerta de preco minimo, o valor alvo deve ser menor ou igual ao preco atual de "
                    + precoAtual + ".");
        }
    }

    private AlertaResponseDTO toResponse(Alerta alerta, BigDecimal precoAtual) {
        return AlertaResponseDTO.builder()
                .id(alerta.getId())
                .ticker(alerta.getAtivo().getTicker())
                .tipo(alerta.getTipo())
                .valorAlvo(alerta.getValorAlvo())
                .precoAtual(precoAtual)
                .notificarWhatsapp(alerta.getNotificarWhatsapp())
                .ativado(alerta.getAtivado())
                .dataCriacao(alerta.getDataCriacao())
                .build();
    }
}
