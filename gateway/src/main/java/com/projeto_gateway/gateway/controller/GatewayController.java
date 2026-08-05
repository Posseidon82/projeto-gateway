package com.projeto_gateway.gateway.controller;

import com.projeto_gateway.gateway.security.JwtTokenProvider; // se necessário
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GatewayController {

    private final RestTemplate restTemplate; // injetado via construtor
    @Value("${backend.url}")
    private String backendUrl;

    @Operation(summary = "Listar pedidos", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "429", description = "Muitas requisições")
    })
    @GetMapping("/pedidos")
    public ResponseEntity<?> forwardPedidos() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Authenticated", "true");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            headers.set("X-Authenticated-User", auth.getName());
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                backendUrl + "/api/pedidos",
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}