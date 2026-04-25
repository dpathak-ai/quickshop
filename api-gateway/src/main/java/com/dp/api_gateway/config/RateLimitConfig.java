package com.dp.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        // Rate limit by IP address
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }

    @Bean
    @Primary
    public KeyResolver authKeyResolver() {
        // Rate limit by JWT subject (email) for auth endpoints
        return exchange -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extract email from token for per-user rate limiting
                return Mono.just(authHeader.substring(7, 27)); // first 20 chars as key
            }
            return Mono.just("anonymous");
        };
    }
}