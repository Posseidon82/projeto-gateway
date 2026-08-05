package com.projeto_gateway.gateway.controller;

import com.projeto_gateway.gateway.dto.LoginRequest;
import com.projeto_gateway.gateway.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    @Operation(summary = "Autenticar e gerar token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        // Simulação: validar credenciais fixas
        if ("admin".equals(loginRequest.getUsername()) && "admin".equals(loginRequest.getPassword())) {
            String token = tokenProvider.generateToken(loginRequest.getUsername(), "ADMIN,USER");
            log.info("Login bem-sucedido para usuário: {}", loginRequest.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        }
        log.warn("Tentativa de login falhou para usuário: {}", loginRequest.getUsername());
        throw new RuntimeException("Credenciais inválidas");
    }
}