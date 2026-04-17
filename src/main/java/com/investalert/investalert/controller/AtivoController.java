package com.investalert.investalert.controller;

import com.investalert.investalert.dto.request.AtivoRequestDTO;
import com.investalert.investalert.dto.response.AtivoResponseDTO;
import com.investalert.investalert.dto.response.HistoricoAtivoResponseDTO;
import com.investalert.investalert.service.AtivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Ativos")
@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
public class AtivoController {

    private final AtivoService ativoService;

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> cadastrar(@RequestBody @Valid AtivoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ativoService.cadastrar(dto));
    }

    @GetMapping
    public ResponseEntity<List<AtivoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(ativoService.listarTodos());
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<AtivoResponseDTO> buscarPorTicker(@PathVariable String ticker) {
        return ResponseEntity.ok(ativoService.buscarPorTicker(ticker));
    }

    @Operation(summary = "Histórico de preços do ativo", description = "Períodos: 7d, 1m, 3m, 6m, 1a, 5a")
    @GetMapping("/{ticker}/historico")
    public ResponseEntity<HistoricoAtivoResponseDTO> historico(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "1m") String periodo) {
        return ResponseEntity.ok(ativoService.buscarHistorico(ticker, periodo));
    }
}
