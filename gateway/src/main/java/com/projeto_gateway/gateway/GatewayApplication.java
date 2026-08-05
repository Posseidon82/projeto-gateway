package com.projeto_gateway.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@SpringBootApplication
@EnableCaching
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RateLimiterConfig rateLimiterConfig() {
		return RateLimiterConfig.custom()
				.limitForPeriod(10)                // 10 requisições
				.limitRefreshPeriod(Duration.ofMinutes(1)) // a cada 1 minuto
				.timeoutDuration(Duration.ZERO)    // não espera, rejeita imediatamente
				.build();
	}

}
