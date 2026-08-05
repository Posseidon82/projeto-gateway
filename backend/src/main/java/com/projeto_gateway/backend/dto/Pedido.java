package com.projeto_gateway.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pedido {
    private Long id;
    private String descricao;
    private Double valor;
}