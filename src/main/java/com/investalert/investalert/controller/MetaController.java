package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.MetaMovimentacaoRequestDTO;
import com.investalert.investalert.dto.request.MetaRequestDTO;
import com.investalert.investalert.dto.response.MetaMovimentacaoResponseDTO;
import com.investalert.investalert.dto.response.MetaResponseDTO;
import com.investalert.investalert.service.MetaMovimentacaoService;
import com.investalert.investalert.service.MetaService;
import com.investalert.investalert.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;
    private final MetaMovimentacaoService metaMovimentacaoService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<MetaResponseDTO> criar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MetaRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(usuarioId, dto));
    }

    @GetMapping
    public ResponseEntity<List<MetaResponseDTO>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(metaService.listarPorUsuario(usuarioId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetaResponseDTO> atualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid MetaRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(metaService.atualizar(id, usuarioId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long usuarioId = getUsuarioId(userDetails);
        metaService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/movimentacoes")
    public ResponseEntity<MetaMovimentacaoResponseDTO> criarMovimentacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody @Valid MetaMovimentacaoRequestDTO dto) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(metaMovimentacaoService.criar(id, usuarioId, dto));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<List<MetaMovimentacaoResponseDTO>> listarMovimentacoes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long usuarioId = getUsuarioId(userDetails);
        return ResponseEntity.ok(metaMovimentacaoService.listarPorMeta(id, usuarioId));
    }

    @DeleteMapping("/{metaId}/movimentacoes/{movimentacaoId}")
    public ResponseEntity<Void> deletarMovimentacao(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long metaId,
            @PathVariable Long movimentacaoId) {

        Long usuarioId = getUsuarioId(userDetails);
        metaMovimentacaoService.deletar(metaId, movimentacaoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long getUsuarioId(UserDetails userDetails) {
        return usuarioService.buscarEntidadePorEmail(userDetails.getUsername()).getId();
    }
}
