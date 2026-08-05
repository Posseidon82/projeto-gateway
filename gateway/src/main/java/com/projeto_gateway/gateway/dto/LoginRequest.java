package com.projeto_gateway.gateway.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}