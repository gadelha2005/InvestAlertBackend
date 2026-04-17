package com.investalert.investalert.controller;

import com.investalert.investalert.config.security.UserPrincipal;
import com.investalert.investalert.dto.request.MetaMovimentacaoRequestDTO;
import com.investalert.investalert.dto.request.MetaRequestDTO;
import com.investalert.investalert.dto.response.MetaMovimentacaoResponseDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.service.MetaMovimentacaoService;
import com.investalert.investalert.service.MetaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Metas Financeiras")
@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;
    private final MetaMovimentacaoService metaMovimentacaoService;

    @PostMapping
    public ResponseEntity<MetaResponseDTO> criar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid MetaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(principal.getUsuarioId(), dto));
    }

    @GetMapping
    public ResponseEntity<List<MetaResponseDTO>> listar(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(metaService.listarPorUsuario(principal.getUsuarioId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaResponseDTO> atualizar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody @Valid MetaRequestDTO dto) {

        return ResponseEntity.ok(metaService.atualizar(id, principal.getUsuarioId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        metaService.deletar(id, principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/movimentacoes")
    public ResponseEntity<MetaMovimentacaoResponseDTO> criarMovimentacao(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody @Valid MetaMovimentacaoRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metaMovimentacaoService.criar(id, principal.getUsuarioId(), dto));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<List<MetaMovimentacaoResponseDTO>> listarMovimentacoes(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(metaMovimentacaoService.listarPorMeta(id, principal.getUsuarioId()));
    }

    @DeleteMapping("/{metaId}/movimentacoes/{movimentacaoId}")
    public ResponseEntity<Void> deletarMovimentacao(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long metaId,
            @PathVariable Long movimentacaoId) {

        metaMovimentacaoService.deletar(metaId, movimentacaoId, principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }
}
