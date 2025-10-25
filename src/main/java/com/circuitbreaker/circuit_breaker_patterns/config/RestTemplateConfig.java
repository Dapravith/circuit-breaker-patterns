package com.circuitbreaker.circuit_breaker_patterns.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestTemplateConfig {

    @Bean
    public WebClient webClient(@Value("${external-api.base-url:https://jsonplaceholder.typicode.com}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
