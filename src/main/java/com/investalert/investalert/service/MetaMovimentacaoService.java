package com.investalert.investalert.service;

import com.investalert.investalert.dto.request.MetaMovimentacaoRequestDTO;
import com.investalert.investalert.dto.response.MetaMovimentacaoResponseDTO;
import com.investalert.investalert.exception.BusinessException;
import com.investalert.investalert.exception.ResourceNotFoundException;
import com.investalert.investalert.exception.UnauthorizedException;
import com.investalert.investalert.model.Meta;
import com.investalert.investalert.model.MetaMovimentacao;
import com.investalert.investalert.model.enums.TipoAcompanhamentoMeta;
import com.investalert.investalert.model.enums.TipoMovimentacaoMeta;
import com.investalert.investalert.repository.MetaMovimentacaoRepository;
import com.investalert.investalert.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaMovimentacaoService {

    private final MetaRepository metaRepository;
    private final MetaMovimentacaoRepository metaMovimentacaoRepository;

    @Transactional
    public MetaMovimentacaoResponseDTO criar(Long metaId, Long usuarioId, MetaMovimentacaoRequestDTO dto) {
        Meta meta = buscarMetaDoUsuario(metaId, usuarioId);
        validarMetaManual(meta);

        BigDecimal novoValorAtual = aplicarMovimentacao(meta.getValorAtual(), dto.getTipo(), dto.getValor());

        MetaMovimentacao movimentacao = MetaMovimentacao.builder()
                .meta(meta)
                .usuario(meta.getUsuario())
                .tipo(dto.getTipo())
                .valor(dto.getValor())
                .descricao(dto.getDescricao())
                .dataMovimentacao(dto.getDataMovimentacao())
                .build();

        meta.setValorAtual(novoValorAtual);
        metaRepository.save(meta);

        return toResponse(metaMovimentacaoRepository.save(movimentacao));
    }

    @Transactional(readOnly = true)
    public List<MetaMovimentacaoResponseDTO> listarPorMeta(Long metaId, Long usuarioId) {
        buscarMetaDoUsuario(metaId, usuarioId);

        return metaMovimentacaoRepository.findByMetaIdOrderByDataMovimentacaoDescIdDesc(metaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deletar(Long metaId, Long movimentacaoId, Long usuarioId) {
        Meta meta = buscarMetaDoUsuario(metaId, usuarioId);
        validarMetaManual(meta);

        MetaMovimentacao movimentacao = metaMovimentacaoRepository.findByIdAndMetaId(movimentacaoId, metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentacao da meta", movimentacaoId));

        metaMovimentacaoRepository.delete(movimentacao);
        recalcularValorAtual(meta);
        metaRepository.save(meta);
    }

    private Meta buscarMetaDoUsuario(Long metaId, Long usuarioId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta", metaId));

        if (!meta.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Sem permissao para acessar esta meta");
        }

        return meta;
    }

    private void validarMetaManual(Meta meta) {
        if (meta.getTipoAcompanhamento() != TipoAcompanhamentoMeta.MANUAL) {
            throw new BusinessException("Movimentacoes manuais so podem ser registradas em metas do tipo MANUAL");
        }
    }

    private BigDecimal aplicarMovimentacao(BigDecimal valorAtual, TipoMovimentacaoMeta tipo, BigDecimal valor) {
        BigDecimal base = valorAtual == null ? BigDecimal.ZERO : valorAtual;
        BigDecimal novoValorAtual = tipo == TipoMovimentacaoMeta.APORTE
                ? base.add(valor)
                : base.subtract(valor);

        if (novoValorAtual.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("A movimentacao nao pode deixar o valor atual da meta negativo");
        }

        return novoValorAtual;
    }

    private void recalcularValorAtual(Meta meta) {
        BigDecimal valorAtual = metaMovimentacaoRepository.findByMetaId(meta.getId()).stream()
                .map(movimentacao -> movimentacao.getTipo() == TipoMovimentacaoMeta.APORTE
                        ? movimentacao.getValor()
                        : movimentacao.getValor().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (valorAtual.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("As movimentacoes da meta resultaram em valor atual negativo");
        }

        meta.setValorAtual(valorAtual);
    }

    private MetaMovimentacaoResponseDTO toResponse(MetaMovimentacao movimentacao) {
        return MetaMovimentacaoResponseDTO.builder()
                .id(movimentacao.getId())
                .metaId(movimentacao.getMeta().getId())
                .tipo(movimentacao.getTipo())
                .valor(movimentacao.getValor())
                .descricao(movimentacao.getDescricao())
                .dataMovimentacao(movimentacao.getDataMovimentacao())
                .dataCriacao(movimentacao.getDataCriacao())
                .build();
    }
}
