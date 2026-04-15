package com.investalert.investalert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "InvestAlert API is running",
                "version", "0.0.1"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "investalert-backend"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
                "status", "OPERATIONAL",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
