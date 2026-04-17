package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.MetaRequestDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Meta;
import com.investalert.investalert.model.Usuario;
import com.investalert.investalert.model.enums.TipoAcompanhamentoMeta;
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
    private final CarteiraService carteiraService;
    private final UsuarioService usuarioService;

    @Transactional
    public MetaResponseDTO criar(Long usuarioId, MetaRequestDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);

        Meta meta = Meta.builder()
                .usuario(usuario)
                .nome(dto.getNome())
                .valorObjetivo(dto.getValorObjetivo())
                .tipoAcompanhamento(resolveTipoAcompanhamento(dto))
                .carteiraId(resolveCarteiraId(dto, usuarioId))
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
        meta.setTipoAcompanhamento(resolveTipoAcompanhamento(dto));
        meta.setCarteiraId(resolveCarteiraId(dto, usuarioId));
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
        BigDecimal valorAtual = resolveValorAtual(meta);
        BigDecimal percentual = null;

        if (meta.getValorObjetivo().compareTo(BigDecimal.ZERO) != 0) {
            percentual = valorAtual
                    .divide(meta.getValorObjetivo(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return MetaResponseDTO.builder()
                .id(meta.getId())
                .nome(meta.getNome())
                .valorObjetivo(meta.getValorObjetivo())
                .valorAtual(valorAtual)
                .tipoAcompanhamento(meta.getTipoAcompanhamento())
                .carteiraId(meta.getCarteiraId())
                .percentualConcluido(percentual)
                .dataCriacao(meta.getDataCriacao())
                .dataLimite(meta.getDataLimite())
                .build();
    }

    private TipoAcompanhamentoMeta resolveTipoAcompanhamento(MetaRequestDTO dto) {
        TipoAcompanhamentoMeta tipo = dto.getTipoAcompanhamento() == null
                ? TipoAcompanhamentoMeta.MANUAL
                : dto.getTipoAcompanhamento();

        if (tipo == TipoAcompanhamentoMeta.MANUAL && dto.getCarteiraId() != null) {
            throw new BusinessException("Carteira vinculada so pode ser informada para metas do tipo CARTEIRA_VINCULADA");
        }

        if (tipo == TipoAcompanhamentoMeta.CARTEIRA_VINCULADA && dto.getCarteiraId() == null) {
            throw new BusinessException("Carteira vinculada e obrigatoria quando o tipo de acompanhamento for CARTEIRA_VINCULADA");
        }

        return tipo;
    }

    private Long resolveCarteiraId(MetaRequestDTO dto, Long usuarioId) {
        if (resolveTipoAcompanhamento(dto) != TipoAcompanhamentoMeta.CARTEIRA_VINCULADA) {
            return null;
        }

        return carteiraService.buscarPorId(dto.getCarteiraId(), usuarioId).getId();
    }

    private BigDecimal resolveValorAtual(Meta meta) {
        if (meta.getTipoAcompanhamento() != TipoAcompanhamentoMeta.CARTEIRA_VINCULADA) {
            return meta.getValorAtual() == null ? BigDecimal.ZERO : meta.getValorAtual();
        }

        if (meta.getCarteiraId() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal valorTotal = carteiraService
                .buscarPorId(meta.getCarteiraId(), meta.getUsuario().getId())
                .getValorTotal();

        BigDecimal valorAtual = valorTotal == null ? BigDecimal.ZERO : valorTotal;
        meta.setValorAtual(valorAtual);
        metaRepository.save(meta);
        return valorAtual;
    }
}
