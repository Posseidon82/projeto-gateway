# 📘 Gateway de API Seguro para Microsserviços

Este projeto implementa um **API Gateway** robusto e seguro para microsserviços, utilizando **Spring Boot 3** e **Spring Security 6**. Ele centraliza autenticação (JWT), autorização (RBAC), rate limiting, proteção contra vulnerabilidades comuns (XSS, CSRF, DoS) e garante comunicação criptografada via HTTPS.

---

## 🚀 Visão Geral

A solução é composta por duas aplicações:

- **Gateway (porta 8443 HTTPS):**  
  Único ponto de entrada. Valida tokens JWT, aplica rate limiting (Resilience4j), adiciona cabeçalhos de segurança e roteia requisições autenticadas para o backend.

- **Backend (porta 8081 HTTP):**  
  Microsserviço mockado que **rejeita** requisições que não contenham o cabeçalho `X-Gateway-Authenticated: true`, garantindo que apenas o gateway possa acessá-lo.

---

## ✅ Pré‑requisitos

- **JDK 21** (ou superior)
- **Maven 3.8+**
- **Git** (opcional, para clonar)
- **curl** ou **Postman** para testes

---

## 🔧 Configuração Inicial

### 1. Gerar o keystore para HTTPS

No diretório `gateway/src/main/resources`, execute:

```bash
keytool -genkeypair -alias gateway -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore keystore.p12 -validity 3650 \
  -dname "CN=localhost, OU=Dev, O=Empresa, L=Cidade, S=Estado, C=BR" \
  -storepass changeit -keypass changeit
```
A senha changeit está configurada no application.yml; altere se necessário.

### 2. Gerar uma chave secreta JWT de 256 bits (Base64)

A chave no `application.yml` deve ter no mínimo 32 bytes (256 bits) para o algoritmo HMAC-SHA256. Gere uma com:

```bash
openssl rand -base64 32
```

Exemplo de saída: `dGhpcy1pcy1hLXN1cGVyLXNlY3JldC1rZXktd2l0aC0zMi1ieXRlcw==`

Copie essa string e cole no lugar de `${JWT_SECRET}` no `application.yml` (ou defina a variável de ambiente `JWT_SECRET`).

### 3. Verificar o arquivo `application.yml`

O arquivo já está configurado com SSL ativo e porta 8443. Certifique-se de que a linha `jwt.secret` contenha sua chave Base64.

---

## 🏃 Como Executar

### Passo 1: Build do projeto (raiz)

```bash
mvn clean install -DskipTests
```

### Passo 2: Iniciar o Backend

```bash
cd backend
mvn spring-boot:run
```

O backend estará disponível em `http://localhost:8081`.

### Passo 3: Iniciar o Gateway (em outro terminal)

```bash
cd gateway
mvn spring-boot:run
```

O gateway estará disponível em `https://localhost:8443`.

---

## 📡 Testes com curl

### 🔐 1. Obter token JWT (login)

```bash
curl -k -X POST https://localhost:8443/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Resposta (sucesso):

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
Guarde o token para os próximos testes.

### ✅ 2. Listar pedidos (com token válido)

```bash
curl -k -X GET https://localhost:8443/api/pedidos \
  -H "Authorization: Bearer <seu_token>"
```

Resposta (sucesso):

```json
[
  {"id":1,"descricao":"Produto A","valor":100.0},
  {"id":2,"descricao":"Produto B","valor":200.0}
]
```

### ❌ 3. Sem token (401 Unauthorized)

```bash
curl -k -X GET https://localhost:8443/api/pedidos
```

Resposta:

```json
{
  "error": "Não autorizado",
  "message": "Token inválido ou ausente"
}
```

### ❌ 4. Token inválido (assinatura errada)

```bash
curl -k -X GET https://localhost:8443/api/pedidos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiJ9.invalido"
```

Resposta: (401) com mensagem genérica.

### ❌ 5. Token expirado

Simule um token com expiração curta (para teste, reduza o tempo de expiração no `JwtTokenProvider`). Ao usar um token expirado, a resposta será:

```json
{
  "error": "Não autorizado",
  "message": "Credenciais inválidas ou token expirado."
}
```

### ⏱️ 6. Excesso de requisições (Rate Limiting)

O gateway limita a 10 requisições por minuto por IP ou usuário. Execute o mesmo comando de listagem 11 vezes em menos de 1 minuto. Na 11ª, você receberá:

```http
HTTP/1.1 429 Too Many Requests
```

Resposta:

```json
{
  "error": "Too Many Requests",
  "message": "Limite de requisições excedido."
}
```

### 🔒 7. Backend rejeita requisição direta (sem gateway)

Se tentar acessar o backend diretamente (porta 8081), ele rejeitará por falta do cabeçalho `X-Gateway-Authenticated`:

```bash
curl -X GET http://localhost:8081/api/pedidos
```

Resposta: `403 Forbidden` (sem corpo, conforme implementado).

---

## 🌐 Swagger UI

A documentação interativa (OpenAPI) está disponível em:

```text
https://localhost:8443/swagger-ui.html
```
1.Faça login via endpoint `/auth/login` e copie o token.
2.Clique em Authorize, cole `Bearer <token>`.
3.Agora você pode testar os endpoints protegidos diretamente pela interface.

---

## 🛡️ Decisões de Segurança (Justificativa Técnica)

| Controle	| Implementação |	Justificativa |
|-----------|---------------|---------------|
| Autenticação JWT	| `JwtTokenProvider` com HMAC-SHA256, validação de emissor e expiração.	| Stateless, escalável, evita armazenamento de sessão no servidor. |
| Autorização (RBAC)	| Filtro com `hasAuthority("ROLE_USER")` para endpoints `/api/pedidos`.	| Garante que apenas usuários com a role adequada acessem recursos sensíveis. |
| Rate Limiting	| Resilience4j com limite de 10 req/min por IP ou usuário.	| Mitiga ataques de DoS e uso abusivo da API. |
| HTTPS obrigatório	| `server.ssl.*` e `spring.security.require-ssl=true`.	| Protege dados em trânsito contra interceptação (MITM). |
| Cabeçalhos de segurança	| CSP, HSTS, X-Content-Type-Options, X-Frame-Options.	| Previne XSS, clickjacking e sniffing de MIME. |
| Tratamento de erros	| `@ControllerAdvice` com respostas JSON genéricas (sem stack traces).	| Evita vazamento de informações internas (OWASP Top 10). |
| Validação no backend	| Cabeçalho `X-Gateway-Authenticated` exigido.	| Garante que o backend só aceite tráfego proveniente do gateway. |
| CSRF desabilitado	| `csrf().disable()` (API stateless).	| APIs REST não utilizam cookies de sessão, eliminando vetores CSRF. |

---

## 🔄 Arquitetura Simplificada

```text
[Cliente] --HTTPS--> [Gateway (8443)]
    | (JWT + headers)
    v
[Backend (8081)]  <-- (valida cabeçalho X-Gateway-Authenticated)
```

---

## 🧪 Exemplo de Coleção Postman

<details> <summary><b>Clique para ver o JSON da coleção (resumido)</b></summary>

```json
{
  "info": {
    "name": "Gateway Seguro",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "url": "https://localhost:8443/auth/login",
        "header": [{"key":"Content-Type","value":"application/json"}],
        "body": {"mode":"raw","raw":"{\"username\":\"admin\",\"password\":\"admin\"}"}
      }
    },
    {
      "name": "Listar Pedidos (com token)",
      "request": {
        "method": "GET",
        "url": "https://localhost:8443/api/pedidos",
        "header": [{"key":"Authorization","value":"Bearer {{token}}"}]
      }
    }
  ]
}
```
</details>

---

## 📁 Estrutura de Pacotes (Gateway)

```text
com.projeto_gateway.gateway/
├── GatewayApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   ├── OpenApiConfig.java
│   └── Resilience4jConfig.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── CustomAuthenticationEntryPoint.java
│   └── CustomAccessDeniedHandler.java
├── controller/
│   ├── GatewayController.java
│   └── AuthController.java
├── interceptor/
│   └── RateLimitingInterceptor.java
├── dto/
│   ├── LoginRequest.java
│   └── ErrorResponse.java
└── exception/
    └── GlobalExceptionHandler.java
```

---

## 📝 Observações Finais

- **O gateway não expõe o backend diretamente; todo o tráfego passa pelo gateway.**
- **O Rate Limiting é aplicado por IP ou usuário (conforme autenticação), usando Resilience4j.**
- **A aplicação está pronta para produção, bastando ajustar o keystore para um certificado válido e configurar variáveis de ambiente.**
- **Para desativar o HTTPS em desenvolvimento, altere spring.security.require-ssl: false e comente a seção ssl no application.yml.**
