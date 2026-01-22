package com.skistation.gateway.securityConfig;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter logRequestHeaders() {
        return (exchange, chain) -> {
            System.out.println("Headers sent to MS: " + exchange.getRequest().getHeaders());
            return chain.filter(exchange);
        };
    }
}
