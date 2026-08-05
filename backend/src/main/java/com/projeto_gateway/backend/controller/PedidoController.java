package com.projeto_gateway.backend.controller;

import com.projeto_gateway.backend.dto.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class PedidoController {

    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> listarPedidos(
            @RequestHeader(value = "X-Gateway-Authenticated", required = false) String authHeader) {

        if (!"true".equals(authHeader)) {
            log.warn("Requisição rejeitada: cabeçalho X-Gateway-Authenticated ausente ou inválido");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Pedidos solicitados via gateway");
        return ResponseEntity.ok(List.of(
                new Pedido(1L, "Produto A", 100.0),
                new Pedido(2L, "Produto B", 200.0),
                new Pedido(3L, "Produto C", 300.0)
        ));
    }
}