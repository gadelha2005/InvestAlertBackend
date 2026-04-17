package com.investalert.investalert.controller;

import com.investalert.investalert.config.security.UserPrincipal;
import com.investalert.investalert.dto.request.CarteiraAtivoRequestDTO;
import com.investalert.investalert.dto.request.CarteiraRequestDTO;
import com.investalert.investalert.dto.response.CarteiraAtivoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraHistoricoResponseDTO;
import com.investalert.investalert.dto.response.CarteiraResponseDTO;
import com.investalert.investalert.service.CarteiraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Carteira")
@RestController
@RequestMapping("/api/carteiras")
@RequiredArgsConstructor
public class CarteiraController {

    private final CarteiraService carteiraService;

    @Operation(summary = "Criar carteira")
    @PostMapping
    public ResponseEntity<CarteiraResponseDTO> criar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid CarteiraRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(carteiraService.criar(principal.getUsuarioId(), dto));
    }

    @Operation(summary = "Listar carteiras do usuário")
    @GetMapping
    public ResponseEntity<List<CarteiraResponseDTO>> listar(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(carteiraService.listarPorUsuario(principal.getUsuarioId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarteiraResponseDTO> buscarPorId(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(carteiraService.buscarPorId(id, principal.getUsuarioId()));
    }

    @Operation(summary = "Adicionar ativo à carteira")
    @PostMapping("/{id}/ativos")
    public ResponseEntity<CarteiraAtivoResponseDTO> adicionarAtivo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody @Valid CarteiraAtivoRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carteiraService.adicionarAtivo(id, principal.getUsuarioId(), dto));
    }

    @Operation(summary = "Remover ativo da carteira")
    @DeleteMapping("/{id}/ativos/{carteiraAtivoId}")
    public ResponseEntity<Void> removerAtivo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @PathVariable Long carteiraAtivoId) {

        carteiraService.removerAtivo(id, carteiraAtivoId, principal.getUsuarioId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Histórico de eventos da carteira")
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<CarteiraHistoricoResponseDTO>> historico(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(carteiraService.buscarHistorico(id, principal.getUsuarioId()));
    }
}