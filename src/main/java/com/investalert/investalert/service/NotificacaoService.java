package com.investalert.investalert.service;

import com.investalert.investalert.dto.response.NotificacaoResponseDTO;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Notificacao;
import com.investalert.investalert.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByDataEnvioDesc(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarNaoLidas(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdAndLidaFalse(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long notificacaoId, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação", notificacaoId));

        if (!notificacao.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para acessar esta notificação");
        }

        notificacao.setLida(true);
        return toResponse(notificacaoRepository.save(notificacao));
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioIdAndLidaFalse(usuarioId);
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
    }

    private NotificacaoResponseDTO toResponse(Notificacao notificacao) {
        return NotificacaoResponseDTO.builder()
                .id(notificacao.getId())
                .mensagem(notificacao.getMensagem())
                .canal(notificacao.getCanal())
                .lida(notificacao.getLida())
                .dataEnvio(notificacao.getDataEnvio())
                .build();
    }
}