package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.MetaRequestDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Meta;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public MetaResponseDTO criar(Long usuarioId, MetaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorEmail(
                usuarioService.buscarPorId(usuarioId).getEmail()
        );

        Meta meta = Meta.builder()
                .usuario(usuario)
                .nome(dto.getNome())
                .valorObjetivo(dto.getValorObjetivo())
                .dataLimite(dto.getDataLimite())
                .build();

        return toResponse(metaRepository.save(meta));
    }

    @Transactional(readOnly = true)
    public List<MetaResponseDTO> listarPorUsuario(Long usuarioId) {
        return metaRepository.findByUsuarioIdOrderByDataLimiteAsc(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MetaResponseDTO atualizar(Long metaId, Long usuarioId, MetaRequestDTO dto) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta", metaId));

        if (!meta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para alterar esta meta");
        }

        meta.setNome(dto.getNome());
        meta.setValorObjetivo(dto.getValorObjetivo());
        meta.setDataLimite(dto.getDataLimite());

        return toResponse(metaRepository.save(meta));
    }

    @Transactional
    public void deletar(Long metaId, Long usuarioId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta", metaId));

        if (!meta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissão para deletar esta meta");
        }

        metaRepository.delete(meta);
    }

    private MetaResponseDTO toResponse(Meta meta) {
        BigDecimal percentual = null;

        if (meta.getValorObjetivo().compareTo(BigDecimal.ZERO) != 0) {
            percentual = meta.getValorAtual()
                    .divide(meta.getValorObjetivo(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return MetaResponseDTO.builder()
                .id(meta.getId())
                .nome(meta.getNome())
                .valorObjetivo(meta.getValorObjetivo())
                .valorAtual(meta.getValorAtual())
                .percentualConcluido(percentual)
                .dataCriacao(meta.getDataCriacao())
                .dataLimite(meta.getDataLimite())
                .build();
    }
}